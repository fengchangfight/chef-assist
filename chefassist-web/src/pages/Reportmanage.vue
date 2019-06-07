<template>
  <q-page style="margin-top:10px;" padding class="docs-table">
      <div class="row justify-center" >
         <h4>全部历史订单</h4>
      </div>
      <q-table
      :data="orders"
      :columns="columns"
      row-key="id"
      color="secondary"
      :pagination.sync="serverPagination"
      @request="request"
      :loading="loading"
      >
      <template slot="top-right" slot-scope="props">
        <q-btn v-if="props!=null" title="刷新订单" round color="blue" @click="refreshItems" icon="refresh" />
      </template>
      <q-td slot="body-cell-OrderStatus" slot-scope="props" :props="props">
        <q-chip v-if="props.value==='finished'" small color="green">已完成</q-chip>
        <q-chip v-else small color="green">props.value</q-chip>
      </q-td>
      <q-td slot="body-cell-OrderNumber" slot-scope="props" :props="props">
        <a href="javascript:" @click.prevent="goPage('/order-detail/'+props.row.id)">{{props.value}}</a>
      </q-td>
     </q-table>
  </q-page>
</template>

<style>
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'OrderHistory',
  data () {
    return {
      table_page_size: 100,
      loading: false,
      orders: [],
      serverPagination: {
        page: 1,
        rowsPerPage: 100,
        rowsNumber: 10 // specifying this determines pagination is server-side
      },
      columns: [
        {
          name: 'OrderNumber',
          required: true,
          label: '订单号',
          align: 'left',
          field: 'order_number',
          sortable: false
        },
        {
          name: 'CreatedBy',
          required: true,
          label: '创建人',
          align: 'left',
          field: 'created_by',
          sortable: false
        },
        {
          name: 'OrderStatus',
          required: true,
          label: '订单状态',
          align: 'left',
          field: 'order_status',
          sortable: false
        },
        {
          name: 'StartTime',
          required: true,
          label: '创建时间',
          align: 'left',
          field: 'start_time',
          sortable: false
        },
        {
          name: 'EndTime',
          required: true,
          label: '完成时间',
          align: 'left',
          field: 'end_time',
          sortable: false
        }
      ]
    }
  },
  mixins: [base],
  mounted () {
    this.loadHistoryOrders()
  },
  methods: {
    refreshItems () {
      this.loadHistoryOrders()
    },
    request (props) {
      this.serverPagination = props.pagination
      this.loadHistoryOrders()
    },
    loadHistoryOrders () {
      var params = {}
      params.page = this.serverPagination.page
      params.pageSize = this.serverPagination.rowsPerPage
      this.loading = true
      this.$axios.get('/api/v1/order/history', {
        params: params
      }).then(response => {
        this.orders = response.data.data
        this.serverPagination.page = response.data.currentPage
        this.serverPagination.rowsNumber = response.data.total
        this.serverPagination.rowsPerPage = response.data.pageSize
        this.loading = false
      }).catch(e => {

      })
    },
    newOrder () {
      this.$axios.post('/api/v1/order/new').then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
          this.goPage('/order-detail/' + response.data.data)
        }
      }).catch(e => {

      })
    }
  }
}
</script>
