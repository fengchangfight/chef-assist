<template>
  <q-page style="margin-top:10px;" class="row justify-center" >
    <div style="max-width: 90vw;" >
      <div class="row justify-center">
        <h4>点菜分配厨位视图</h4>
      </div>
      <div class="row justify-center" style="margin-bottom:10px;">
        <q-toggle @input="switchOnline" v-model="online_only" color="secondary" label="不显示离线" />
      </div>
      <div class="justify-center" style="margin-left:10px;">
        <q-card v-bind:class="{'offline-card': kit.status==='offline', 'online-card': kit.status==='online', 'blocking-card': kit.status==='blocking', }" v-for="kit in positions" v-bind:key="kit.id" inline class="q-ma-sm" style="max-width: 400px;">
          <q-card-title>
            厨位:{{kit.producer_number}}
            <span slot="subtitle">{{kit.description}}</span>
          </q-card-title>
          <q-list>
            <q-item v-for="l in kit.inner_items" v-bind:key="l.item_id">
              <q-item-side>
                <q-item-tile color="primary" icon="restaurant menu" />
              </q-item-side>
              <q-item-main>
               <q-item-tile label>{{l.dish_name}}-{{l.dish_count}}份-<label style="color:red;">{{l.description}}</label></q-item-tile>
              </q-item-main>
              <q-item-side>
                <q-btn dense @click="ready2serve(l.item_id)" color="primary" label="完成" />
              </q-item-side>
           </q-item>
          </q-list>
          <q-item>
            <q-item-main>
              <label style="color:red;" v-if="kit.status==='online'">在线</label>
              <label style="color:#D200D2;" v-if="kit.status==='blocking'">阻挡</label>
              <label style="color:gray;" v-if="kit.status==='offline'">离线</label>
            </q-item-main>
         </q-item>
         </q-card>
         <q-inner-loading :visible="loading">
           <q-spinner-gears size="50px" color="primary"></q-spinner-gears>
         </q-inner-loading>
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
.offline-card{
    background-color: #E0E0E0;
    opacity:0.8
}
.online-card{
    background-color: #D3FF93;
}
.blocking-card{
    background-color: #FFED97;
}
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'ChefAssignment',
  data () {
    return {
      loading: false,
      online_only: true,
      positions: [],
      total: 0,
      page_size: 1,
      tables: [],
      customDialogModel: false,
      current_page: 1
    }
  },
  mixins: [base],
  mounted () {
    this.loadKPositionsWithItems()
    var storeThis = this
    setInterval(function () {
      storeThis.loadKPositionsWithItems()
    }, 20000)
  },
  methods: {
    switchOnline (val) {
      this.loadKPositionsWithItems()
    },
    ready2serve (orderItemId) {
      this.$axios.put('/api/v1/order-item/finish-cooking/' + orderItemId).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadKPositionsWithItems()
      }).catch(e => {
        this.notifyFail('未知错误ready2serve')
      })
    },
    onOk () {
      console.log('ok')
    },
    onCancel () {
      console.log('cancel')
    },
    onShow () {
      console.log('show')
    },
    onHide () {
      console.log('hide')
    },
    reloadPage (val) {
      this.current_page = val
      this.loadTables()
    },
    showLoadingOneSec () {
      this.loading = true
      setTimeout(() => {
        this.loading = false
      }, 500)
    },
    loadKPositionsWithItems () {
      var params = {}
      params.page = this.current_page
      params.online_only = this.online_only
      this.$axios.get('/api/v1/dish-producer/withitems', {
        params: params
      }).then(response => {
        this.showLoadingOneSec()
        this.positions = response.data.data
        this.current_page = response.data.currentPage
        this.page_size = response.data.pageSize
        this.total = response.data.total
      }).catch(e => {
        console.log(e.response)
        this.notifyFail('获取厨位数据失败')
      })
    }
  }
}
</script>
