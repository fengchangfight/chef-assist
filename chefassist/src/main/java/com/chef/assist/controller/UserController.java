package com.chef.assist.controller;

import com.chef.assist.config.CaResponse;
import com.chef.assist.mapper.RoleMapper;
import com.chef.assist.mapper.UserMapper;
import com.chef.assist.model.Role;
import com.chef.assist.model.User;
import com.chef.assist.model.dto.PaginationWrapper;
import com.chef.assist.model.dto.UserVO;
import com.chef.assist.model.request.AssignRoleRequest;
import com.chef.assist.model.request.ChangepassRequest;
import com.chef.assist.model.request.CreateUserRequest;
import com.chef.assist.utils.MyStringUtil;
import com.chef.assist.utils.SecurityUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private Environment env;

    private String currentUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return currentUserName;
        }else{
            return null;
        }
    }

    @GetMapping("/info")
    public UserVO getMyInfo(){
        UserVO result = userMapper.getUserInfo(currentUserName());
        return result;
    }

    @GetMapping
    public PaginationWrapper getAllUsers(@RequestParam(value = "page", required = true) Integer page,
                                         @RequestParam(value = "filterRoleName") String filterRoleName,
                                         @RequestParam(value = "searchName") String searchName){
        int defaultPageSize = env.getProperty("default.page.size", Integer.class);
        PageHelper.startPage(page,defaultPageSize);
        List<UserVO> users = new ArrayList<>();
        Integer total = userMapper.countUsers();
        if(StringUtils.isEmpty(filterRoleName)){
            if(StringUtils.isEmpty(searchName)){
                users = userMapper.findAllUsers();
            }else{
                users = userMapper.findAllUsersByLikeName(searchName);
                total = userMapper.countByLikeName(searchName);
            }
        }else{
            if(StringUtils.isEmpty(searchName)){
                users = userMapper.findAllUsersByRoleName(filterRoleName);
                total = userMapper.countByRoleName(filterRoleName);
            }else{
                users = userMapper.findAllUsersByRoleNameAndLikeName(filterRoleName, searchName);
                total = userMapper.countByRoleNameAndLikeName(filterRoleName, searchName);
            }
        }

        PaginationWrapper result = new PaginationWrapper();
        result.setData(users);
        result.setCurrentPage(page);
        result.setPageSize(defaultPageSize);
        result.setTotal(total);

        return result;
    }

    /**
     * createUser with random salt and default password, specific role and username(English char only)
     * @param request
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    @PostMapping("/new")
    public CaResponse createUser(@Valid @RequestBody CreateUserRequest request) throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = new User();
        Role role = null;
        String defaultPassword = env.getProperty("user.default.password");
        try{
            role = roleMapper.findByRoleId(request.getRoleId());
            String salt =MyStringUtil.randomAlphaNumeric(10);

            user.setPassword(SecurityUtil.generateStorngPasswordHash(defaultPassword,salt));
            user.setSalt(salt);
            user.setRoleId(request.getRoleId());

            String username = request.getUsername();
            if(!username.matches("[a-zA-Z0-9]*")){
                return CaResponse.makeResponse(false, "用户名只允许字母和数字的组合", null);
            }
            user.setUsername(username);
            userMapper.insert(user);
        }catch (Exception e){
            if(e instanceof DuplicateKeyException){
                return CaResponse.makeResponse(false,"用户名已存在", null);
            }
            return CaResponse.makeResponse(false, "创建用户失败:"+e.getMessage(), null);
        }

        return CaResponse.makeResponse(true, "成功创建"+role.getRoleName()+"角色的用户:"+user.getUsername()+",初始密码:"+defaultPassword, new UserVO(user.getId(), user.getUsername(), role.getRoleName()));
    }

    @PutMapping("/assign-role")
    public CaResponse assignUserRole(@RequestBody AssignRoleRequest assignRoleRequest){
        User user = userMapper.findByUserId(assignRoleRequest.getUserId());
        Role role = roleMapper.findByRoleId(assignRoleRequest.getRoleId());
        if(role==null){
            return CaResponse.makeResponse(false,"找不到对应的role", assignRoleRequest.getRoleId());
        }
        user.setRoleId(assignRoleRequest.getRoleId());
        userMapper.update(user);
        return CaResponse.makeResponse(true, "成功赋予用户:"+user.getUsername()+role.getRoleName()+"角色", Arrays.asList(user.getUsername(),role.getRoleName()));
    }

    @PutMapping("/change-pass")
    public CaResponse changePassword(@Valid @RequestBody ChangepassRequest changepassRequest) throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = userMapper.findByUserName(currentUserName());

        //先检查原密码
        String shouldBePasswordEnc = SecurityUtil.generateStorngPasswordHash(changepassRequest.getPassword(), user.getSalt());
        if(!shouldBePasswordEnc.equals(user.getPassword())){
            return CaResponse.makeResponse(false, "原密码不对哦", user.getUsername());
        }

        if(changepassRequest.getNewPassword().equals(changepassRequest.getPassword())){
            return CaResponse.makeResponse(false, "新密码不能和原密码一样哦", user.getUsername());
        }

        //原密码正确的情况下做以下更新
        String newPassEnc = SecurityUtil.generateStorngPasswordHash(changepassRequest.getNewPassword(), user.getSalt());
        user.setPassword(newPassEnc);

        userMapper.update(user);

        return CaResponse.makeResponse(true, "更新密码成功", user.getUsername());
    }

    @DeleteMapping("/{id}")
    public CaResponse deleteUserById(@PathVariable("id") Long id){
        User adminUser = userMapper.findByUserName("admin");
        if(adminUser.getId().equals(id)){
            return CaResponse.makeResponse(false,"超级管理员用户不能删除!", null);
        }

        userMapper.deleteById(id);
        return CaResponse.makeResponse(true,"成功删除用户", id);
    }

    @PutMapping("/password-reset/{id}")
    public CaResponse resetUserPassword(@PathVariable("id") Long id) throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = userMapper.findByUserId(id);
        String defaultPassword = env.getProperty("user.default.password");
        String origPass = SecurityUtil.generateStorngPasswordHash(defaultPassword,user.getSalt());
        user.setPassword(origPass);
        userMapper.update(user);

        return CaResponse.makeResponse(true,"成功重设用户"+user.getUsername()+"的密码", null);
    }


}
