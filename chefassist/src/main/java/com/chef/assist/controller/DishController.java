package com.chef.assist.controller;

import com.chef.assist.config.CaResponse;
import com.chef.assist.mapper.DishMapper;
import com.chef.assist.mapper.DishTagMapper;
import com.chef.assist.model.Dish;
import com.chef.assist.model.DishTag;
import com.chef.assist.model.dto.DishDTO;
import com.chef.assist.model.dto.PaginationWrapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dish")
public class DishController {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishTagMapper dishTagMapper;

    @Autowired
    private Environment env;

    @GetMapping
    public PaginationWrapper getDishes(@RequestParam(value = "page", required = true) Integer page, @RequestParam(value = "tags") String tags){
        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        PageHelper.startPage(page,defaultPageSize);
        List<Dish> dishes = new ArrayList<>();
        int totalSize = 0;
        if(StringUtils.isEmpty(tags)){
            dishes = dishMapper.getAllDish();
            totalSize = dishMapper.count();
        }else{
            List<String> tagList = Arrays.asList(tags.split(","));
            dishes = dishMapper.getDishByTags(tagList);
            totalSize = dishMapper.countByTags(tagList);
        }

        PaginationWrapper result = new PaginationWrapper(totalSize, defaultPageSize, page, dishes);
        return result;
    }

    @GetMapping("/option")
    public List<DishDTO> getDishesByFilter(@RequestParam(value = "filter_field", required = false) String filterField){
        return dishMapper.getDishList();
    }

    @DeleteMapping("/{id}")
    public CaResponse deleteDish(@PathVariable Long id){
        try{
            dishMapper.delete(id);
            return CaResponse.makeResponse(true, "成功删除菜品", id);
        }catch (Exception e){
            if(e instanceof DataIntegrityViolationException){
                return CaResponse.makeResponse(false, "不能删除该菜品，因其被其他实体引用", id);
            }
            return CaResponse.makeResponse(false, "未知错误", id);
        }
    }

    @PostMapping
    public CaResponse newDish(@Valid @RequestBody Dish dish){
        String rawTags = dish.getTags();

        List<String> tags = new ArrayList<String>(Arrays.asList(rawTags.split(",")));
        List<String> filterTags = tags.stream().filter(r->r.trim().length()>0).collect(Collectors.toList());
        if(filterTags.size()>0){
            dish.setTags(String.join(",",filterTags));
        }else{
            dish.setTags(null);
        }
        try{
            List<DishTag> existingTagObj = new ArrayList<>();
            if(filterTags.size()>0){
                existingTagObj = dishTagMapper.findExistTags(filterTags);
            }
            List<String> existingTagNames = existingTagObj.stream().map(r->{return r.getName();}).collect(Collectors.toList());
            filterTags.removeAll(existingTagNames);

            if(filterTags.size()>0){
                dishTagMapper.insertMany(filterTags);
            }
            // insert dish object
            dishMapper.insert(dish);
            return CaResponse.makeResponse(true, "成功创建菜品:"+dish.getName(), dish.getId());
        }catch (Exception e){
            if(e instanceof DuplicateKeyException){
                return CaResponse.makeResponse(false, "菜品已存在", null);
            }
            return CaResponse.makeResponse(false, "创建菜品失败", null);
        }
    }


    @PutMapping("/{id}")
    public CaResponse editDish(@PathVariable("id") Long id, @Valid @RequestBody Dish dish){
        String rawTags = dish.getTags();

        Dish origin = dishMapper.findById(id);

        origin.setDescription(dish.getDescription());
        origin.setExpectedCookingTime(dish.getExpectedCookingTime());
        origin.setName(dish.getName());
        origin.setPrice(dish.getPrice());

        // insert not existing tags first
        //  String[] tags = rawTags.split(",");
        List<String> tags = new ArrayList<String>(Arrays.asList(rawTags.split(",")));
        List<String> filterTags = tags.stream().filter(r->r.trim().length()>0).collect(Collectors.toList());
        try{
            if(filterTags!=null && filterTags.size()>0){
                origin.setTags(String.join(",",filterTags));
                List<DishTag> existingTagObj = dishTagMapper.findExistTags(filterTags);
                List<String> existingTagNames = existingTagObj.stream().map(r->{return r.getName();}).collect(Collectors.toList());
                filterTags.removeAll(existingTagNames);
                if(filterTags.size()>0){
                    // 集合做差后剩下的tag就是新tag,插入新tag
                    dishTagMapper.insertMany(filterTags);
                }
            }else{
                origin.setTags(null);
            }
            // 更新菜品
            dishMapper.update(origin);
            return CaResponse.makeResponse(true, "成功更新菜品:"+dish.getName(), dish.getId());
        }catch (Exception e){
            return CaResponse.makeResponse(false, "更新菜品失败", null);
        }
    }

    @GetMapping("/tag")
    public List<DishTag> newDishTag(@RequestParam(value = "search_field", required = true) String searchField){
        return dishTagMapper.findAllDishTagsByLikeName(searchField);
    }

    @GetMapping("/{id}")
    public Dish getDishById(@PathVariable("id") Long id){
        Dish dish = dishMapper.findById(id);
        return dish;
    }

    @GetMapping("/tag-all")
    public List<DishTag> newDishTag(){
        return dishTagMapper.findAll();
    }

    private BufferedImage InputImage(MultipartFile file) {
        BufferedImage srcImage = null;
        try {
            FileInputStream in = (FileInputStream) file.getInputStream();
            srcImage = javax.imageio.ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return srcImage;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    @PostMapping("/thumbnail/{id}")
    public CaResponse generateThumbnail4Dish(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) throws IOException {
        BufferedImage img = InputImage(file);
        BufferedImage newBufImg = resize(img, 100, 100);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(newBufImg, "jpg", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        String base64Img = encoder.encode(outputStream.toByteArray());

        String thumbnailBase64 = "data:image/jpeg;base64,"+base64Img;

        // update to database
        Dish dish = dishMapper.findById(id);
        dish.setThumbnail(thumbnailBase64);
        dishMapper.update(dish);

        return CaResponse.makeResponse(true,"成功上传缩略图", thumbnailBase64);
    }
}
