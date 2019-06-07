<template>
  <q-page style="margin-top:10px;" class="row justify-center" >
    <div style="max-width: 90vw;" >
      <div class="row justify-center">
        <h4>历史订单(餐桌)</h4>
      </div>
      <div class="row justify-center">
        <q-card v-for="table in tables" v-bind:key="table.id" inline class="q-ma-sm">
          <q-card-media>
            <img src="statics/table.png">
            <q-card-title slot="overlay">
              {{table.table_number}}
            <span slot="subtitle">{{table.description}}</span>
            </q-card-title>
          </q-card-media>
          <q-card-actions>
            <q-btn icon="assignment" @click="checkHistoryReportByTable(table.id)" flat>查看历史订单</q-btn>
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
  name: 'Reportbytable',
  mixins: [base],
  data () {
    return {
      total: 0,
      page_size: 1,
      tables: [],
      k_table_no: '',
      k_description: '',
      newTableDialog: false,
      editTableDialog: false,
      current_page: 1,
      edit_table_id: ''
    }
  },
  mounted () {
    this.loadTables()
  },
  methods: {
    checkHistoryReportByTable (tableId) {
      this.goPage('/history-order-by-table/' + tableId)
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
    loadTables () {
      var params = {}
      params.page = this.current_page
      this.$axios.get('/api/v1/table', {
        params: params
      }).then(response => {
        this.tables = response.data.data
        this.current_page = response.data.currentPage
        this.page_size = response.data.pageSize
        this.total = response.data.total
      }).catch(e => {

      })
    }
  }
}
</script>
