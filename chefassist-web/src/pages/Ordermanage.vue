<template>
  <q-page style="margin-top:10px;" padding class="docs-table">
      <div class="row justify-center" >
         <h4>进行中订单</h4>
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
      <!-- <template slot="top-right">
        <q-btn title="刷新列表" round color="secondary" @click="refreshItems" style="margin-top:15px;margin-left:10px;" icon="refresh" />
      </template> -->
      <template slot="top-right" slot-scope="props">
        <q-btn v-if="props!=null" title="刷新订单" round color="blue" @click="refreshItems" icon="refresh" />
      </template>
      <template slot="top-left" slot-scope="props">
        <q-btn v-if="props!=null" color="primary" @click="newOrder" icon-right="playlist_add" label="新订单" />
      </template>
      <q-td slot="body-cell-OrderStatus" slot-scope="props" :props="props">
        <q-chip v-if="props.value==='active'" small color="orange">进行中</q-chip>
        <q-chip v-else small color="green">props.value</q-chip>
      </q-td>
      <q-td slot="body-cell-OrderNumber" slot-scope="props" :props="props">
        <a href="javascript:" @click.prevent="goPage('/order-detail/'+props.row.id)">{{props.value}}</a>
      </q-td>
      <q-td style="min-width:150px;" slot="body-cell-Action" slot-scope="props" :props="props">
        <q-btn v-if="props!=null" color="primary" @click="deleteOrderIfNoItems(props.row.id)" dense icon="delete" label="删除订单" />
      </q-td>
     </q-table>
  </q-page>
</template>

<style>
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'Ordermanage',
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
          name: 'Action',
          required: true,
          label: '操作',
          align: 'left',
          sortable: false
        }
      ]
    }
  },
  mixins: [base],
  mounted () {
    this.loadActiveOrders()
  },
  methods: {
    deleteOrderIfNoItems (orderId) {
      this.$q.dialog({
        title: '确认删除订单',
        ok: '是',
        cancel: '否'
      }).then(() => {
        this.$axios.delete('/api/v1/order/active/' + orderId).then(response => {
          if (response.data.ok === true) {
            this.notifySuccess(response.data.message)
          } else {
            this.notifyFail(response.data.message)
          }
          this.loadActiveOrders()
        }).catch(e => {
          this.notifyFail(e.response.data.message)
        })
      }).catch(() => {

      })
    },
    refreshItems () {
      this.loadActiveOrders(this.table_page_size)
    },
    request (props) {
      this.serverPagination = props.pagination
      this.loadActiveOrders(this.table_page_size)
    },
    loadActiveOrders () {
      var params = {}
      params.page = this.serverPagination.page
      params.pageSize = this.serverPagination.rowsPerPage
      this.loading = true
      this.$axios.get('/api/v1/order/active', {
        params: params
      }).then(response => {
        this.orders = response.data.data
        this.serverPagination.page = response.data.currentPage
        this.serverPagination.rowsNumber = response.data.total
        this.serverPagination.rowsPerPage = response.data.pageSize
        this.loading = false
      }).catch(e => {
        this.notifyFail(e.response.data.message)
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
