<template>
  <q-page style="margin-top:10px;" class="row justify-center">
    <div style="max-width: 90vw;" >
      <div class="row justify-center">
        <h4>菜品管理</h4>
      </div>
      <div class="row justify-center">
        <q-btn @click="newDish" color="blue" label="录入菜品" title="录入菜品" icon="playlist_add"/>
      </div>
      <div class="row justify-center">
        <q-select
          multiple
          chips
          @input="selectChange"
          color="purple"
          float-label="按标签过滤"
          v-model="multi_select_tags"
          :options="dish_tags"
        />
      </div>
      <div style="margin-left:10px;" class="justify-center">
        <q-card style="max-width: 200px" v-for="dish in dishes" v-bind:key="dish.id" inline class="q-ma-sm">
          <q-card-media>
            <img :src="dish.thumbnail===null?'/statics/dish.png':dish.thumbnail" style="cursor:pointer;" @click="goPage('/edit-dish/'+dish.id)">
          </q-card-media>
          <q-card-title>
            <a href="javascript:" @click.prevent="goPage('/edit-dish/'+dish.id)">{{dish.name}}({{dish.price}}元)</a>
          </q-card-title>
          <q-card-main>
            {{dish.description}}
          </q-card-main>
          <q-card-separator />
          <q-card-main v-if="!checkStringNull(dish.tags)">
            <q-chip v-for="tag in dish.tags.split(',')" v-bind:key="tag" small color="cyan-6">{{tag}}</q-chip>
          </q-card-main>
          <q-card-separator />
           <q-card-actions>
           <q-btn @click="deleteDish(dish.id)" flat color="primary">删除</q-btn>
          </q-card-actions>
        </q-card>
      </div>
      <div class="row justify-center" style="margin-top:5px;">
        <q-pagination
         v-if="total>0"
         @input="reloadPage"
         v-model="current_page"
         color="blue"
         :min="1"
         :max="Math.floor(total / page_size) + 1"
         boundary-links
       />
      </div>
    </div>
  </q-page>
</template>

<style>
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'Dishmanage',
  mixins: [base],
  data () {
    return {
      dishes: [],
      dish_tags: [],
      multi_select_tags: [],
      current_page: 1,
      total: 0,
      page_size: 1
    }
  },
  mounted () {
    this.loadDish()
    this.loadDishTags()
  },
  methods: {
    deleteDish (id) {
      this.$q.dialog({
        title: '删除确认',
        ok: '是',
        cancel: '否'
      }).then(() => {
        this.$axios.delete('/api/v1/dish/' + id).then(response => {
          if (response.data.ok === true) {
            this.notifySuccess(response.data.message)
          } else {
            this.notifyFail(response.data.message)
          }
          this.loadDish()
        }).catch(e => {
          console.log(e.response.data.message)
          this.notifyFail(e.response.data.message)
        })
      }).catch(() => {

      })
    },
    selectChange (val) {
      this.loadDish()
    },
    loadDishTags () {
      this.$axios.get('/api/v1/dish/tag-all').then(response => {
        var tags = response.data
        this.dish_tags = tags.map(tag => {
          return {
            label: tag.name,
            icon: 'filter_vintage',
            value: tag.name
          }
        })
      }).catch(e => {

      })
    },
    reloadPage (val) {
      this.current_page = val
      this.loadDish()
    },
    loadDish () {
      var params = {}
      params.page = this.current_page
      params.tags = this.multi_select_tags.join(',')
      this.$axios.get('/api/v1/dish', {
        params: params
      }).then(response => {
        this.dishes = response.data.data
        this.current_page = response.data.currentPage
        this.total = response.data.total
        this.page_size = response.data.pageSize
      }).catch(e => {

      })
    },
    newDish () {
      this.goPage('/new-dish')
    }
  }
}
</script>
