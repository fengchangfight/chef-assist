<template>
  <q-page style="margin-top:10px;" class="row justify-center">
    <div style="width: 400px; max-width: 90vw; " >
      <h4>编辑菜品</h4>

      <p class="caption">上传图片(.jpg,.png,.jpeg)</p>
      <q-uploader @finish="finishUploading" extensions=".jpg,.png,.jpeg" :url='upload_url' :with-credentials=true ref="file" />
      <q-item-separator />

      <img :src='thumbnail' />
      <q-field icon="short_text">
        <q-input v-model="dish_name" placeholder="菜名" />
      </q-field>
      <q-field icon="description">
        <q-input v-model="description" placeholder="菜品描述" />
      </q-field>
      <q-field icon="tag_faces">
         <q-chips-input  v-model="dish_tags" placeholder="添加菜品标签"  @duplicate="duplicate">
           <q-autocomplete :min-characters="1" @search="search" @selected="selected" />
         </q-chips-input>
      </q-field>
      <q-field icon="timer" label="平均制作工时(分钟)" :label-width="5">
         <q-input type="number"  max-length="16" v-model="expected_cooking_time" />
      </q-field>
      <q-field icon="timer" label="价格" :label-width="5">
         <q-input type="number"  max-length="16" v-model="price" />
      </q-field>
      <q-field>
        <q-btn  @click="goPage('/dish-manage')"  icon="keyboard return" color="purple-7" label="返回菜品列表" />
        <q-btn  @click="editDish" icon="play arrow" style="margin-left: 10px;" color="blue" label="更新" />
      </q-field>
    </div>
  </q-page>
</template>

<style>
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'Editdish',
  data () {
    return {
      url: this.upload_url,
      expected_cooking_time: 20,
      dish_name: '',
      dish_tags: [],
      dishId: '',
      description: '',
      thumbnail: '',
      base_service_url: '',
      price: 1.0
    }
  },
  mixins: [base],
  mounted () {
    this.getBaseServiceUrl()
    this.dishId = this.$route.params.id
    this.loadDishDetail(this.$route.params.id)
  },
  computed: {
    upload_url: function () {
      return this.base_service_url + '/' + 'api/v1/dish/thumbnail/' + this.$route.params.id
    }
  },
  methods: {
    finishUploading (val) {
      this.notifySuccess('上传完毕')
      this.loadDishDetail(this.dishId)
    },
    loadDishDetail (dishId) {
      this.$axios.get('/api/v1/dish/' + dishId).then(response => {
        this.dish_name = response.data.name
        this.description = response.data.description
        this.thumbnail = response.data.thumbnail
        this.price = response.data.price
        if (response.data.tags === null) {
          this.dish_tags = []
        } else {
          this.dish_tags = response.data.tags.split(',')
        }
        this.expected_cooking_time = response.data.expected_cooking_time
      }).catch(e => {
        console.log(e)
        this.notifyFail(e.response.data.message)
      })
    },
    editDish () {
      if (this.checkStringNull(this.dish_name)) {
        this.notifyWarn('菜名不得为空')
        return
      }
      this.$axios.put('/api/v1/dish/' + this.dishId, {
        name: this.dish_name,
        description: this.description,
        tags: this.dish_tags.join(','),
        expected_cooking_time: this.expected_cooking_time,
        price: this.price
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
          this.goPage('/dish-manage')
        } else {
          this.notifyFail(response.data.message)
        }
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    selected (item) {
      console.log(item.label)
    },
    duplicate (label) {
      this.$q.notify(`"${label}" 重复标签`)
    },
    search (terms, done) {
      var searchParams = {}
      searchParams.search_field = terms
      this.$axios.get('/api/v1/dish/tag', {
        params: searchParams
      }).then(response => {
        let tags = response.data
        var results = tags.map(tag => {
          return {
            label: tag.name,
            icon: 'filter_vintage',
            value: tag.name
          }
        })
        // Call done and pass the result set
        done(results)
      })
    }
  }
}
</script>
