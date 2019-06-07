<template>
  <q-page style="margin-top:10px;" class="row justify-center" >
    <div style="max-width: 90vw;" >
      <div class="row justify-center">
        <h4>餐桌视图</h4>
      </div>
      <div class="justify-center" style="margin-left:10px;">
        <q-card style="max-width:400px;background-color:#77C0C7" v-for="table in tables" v-bind:key="table.id" inline class="q-ma-sm">
          <q-card-title>
            <a href="javascript:" @click.prevent="goPage('/order-detail-by-table/'+table.id)" >桌号:{{table.table_number}}</a>
            <span slot="subtitle">{{table.description}}</span>
          </q-card-title>
          <q-list>
            <q-item v-for="l in table.inner_items" v-bind:key="l.item_id">
              <q-item-side>
                <q-item-tile color="primary" icon="restaurant menu" />
              </q-item-side>
              <q-item-main>
               <q-item-tile label>{{l.dish_name}}-{{l.dish_count}}份-<label style="color:red;">{{l.description}}</label></q-item-tile>
              </q-item-main>
              <q-item-side>
                  <q-chip v-if="l.status==='readytoserve'" small color="deep-orange-6">完成待取</q-chip>
                  <q-chip v-else-if="l.status==='unassigned'" small color="brown-4">未分配</q-chip>
                  <q-chip v-else-if="l.status==='assigned'" small color="lime-14">制作中</q-chip>
                  <q-chip v-else-if="l.status==='served'" small color="light-green-10">已上菜</q-chip>
                  <q-chip v-else small color="brown">{{l.status}}</q-chip>
              </q-item-side>
              <q-item-side>
                <q-btn @click="served(l.item_id)" v-if="l.status==='readytoserve'" icon="speaker_notes_off" dense glossy color="purple-13" label="划菜" />
              </q-item-side>
           </q-item>
          </q-list>
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
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'OrderByTable',
  data () {
    return {
      loading: false,
      total: 0,
      page_size: 1,
      tables: [],
      k_table_no: '',
      k_description: '',
      customDialogModel: false,
      current_page: 1
    }
  },
  mixins: [base],
  mounted () {
    this.loadTables()
    var storeThis = this
    setInterval(function () {
      storeThis.loadTables()
      console.log('refresh table view board')
    }, 20000)
  },
  methods: {
    served (itemId) {
      this.$axios.put('/api/v1/order-item/served/' + itemId).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadTables()
      }).catch(e => {
        this.notifyFail('未知错误served')
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
    loadTables () {
      var params = {}
      params.page = this.current_page
      this.$axios.get('/api/v1/table/withitems', {
        params: params
      }).then(response => {
        this.showLoadingOneSec()
        this.tables = response.data.data
        this.current_page = response.data.currentPage
        this.page_size = response.data.pageSize
        this.total = response.data.total
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    }
  }
}
</script>
