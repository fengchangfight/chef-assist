<template>
  <q-page style="margin-top:10px;" class="row justify-center">
    <div style="max-width: 90vw; margin-top:10px;" >
      <div class="row justify-center">
        <h4>桌号:{{table_no}} 的历史订单</h4>
      </div>
      <div class="row justify-center">
        <q-table
        style="max-width: 90vw;"
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
        <template slot="top-left" slot-scope="props">
          <q-btn v-if="props!=null" label="返回" color="purple-7" icon="keyboard_backspace" @click="goPage('/report-by-table')" />
        </template>
        <template slot="top-right" slot-scope="props">
          <q-btn v-if="props!=null" title="刷新列表" round color="blue" @click="refreshItems" icon="refresh" />
        </template>
        <q-td slot="body-cell-OrderStatus" slot-scope="props" :props="props">
          <q-chip v-if="props.value==='active'" small color="brown-4">进行中</q-chip>
          <q-chip v-else-if="props.value==='finished'" small color="green">已完成</q-chip>
          <q-chip v-else small color="amber">{{props.value}}</q-chip>
        </q-td>
        <q-td slot="body-cell-OrderNumber" slot-scope="props" :props="props">
          <a href="javascript:" @click.prevent="goPage('/order-detail/'+props.row.id)">{{props.value}}</a>
        </q-td>
        <q-td style="min-width:150px;" slot="body-cell-DishCount" slot-scope="props" :props="props">
          <q-btn :disable="props.row.status!=='unassigned'" size="sm" round dense color="secondary" icon="remove" @click="updateUnassignedItemCount(props.row.id, props.value-1)" class="q-mr-xs" />
          <q-btn :disable="props.row.status!=='unassigned'" size="sm" round dense color="tertiary" icon="add" @click="updateUnassignedItemCount(props.row.id, props.value+1)" class="q-mr-sm" />
          {{props.value}}
        </q-td>
        <q-td style="max-width:300px;" slot="body-cell-Description" slot-scope="props" :props="props">
          <span>{{props.value}}</span>
        </q-td>
       </q-table>
      </div>
    </div>
  </q-page>
</template>

<style>
.q-table td, .q-table th {
      /* don't shorten cell contents */
      white-space: normal !important;
}
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'HistoryorderdetailByTable',
  data () {
    return {
      table_no: '',
      tableId: '',
      serverPagination: {
        page: 1,
        rowsPerPage: 100,
        rowsNumber: 10 // specifying this determines pagination is server-side
      },
      loading: false,
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
      ],
      total: 0,
      page_size: 1,
      description: '',
      count: 1,
      table_page_size: 100,
      order_no: '',
      table_options: [],
      table_option: '',
      dish_options: [],
      dish_option: '',
      created_by: '',
      order_items: [],
      orders: []
    }
  },
  mixins: [base],
  mounted () {
    this.table_id = this.$route.params.id
    this.loadHistoryOrderByTable()
    this.loadTableInfo()
  },
  methods: {
    loadHistoryOrderByTable () {
      var params = {}
      params.page = this.serverPagination.page
      params.pageSize = this.serverPagination.rowsPerPage
      this.$axios.get('/api/v1/order/history-by-table/' + this.table_id, {
        params: params
      }).then(response => {
        this.orders = response.data.data
        this.serverPagination.page = response.data.currentPage
        this.serverPagination.rowsNumber = response.data.total
        this.serverPagination.rowsPerPage = response.data.pageSize
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    refreshItems () {
      this.loadHistoryOrderByTable()
    },
    request (props) {
      this.serverPagination = props.pagination
      this.loadHistoryOrderByTable()
    },
    loadTableInfo () {
      this.$axios.get('/api/v1/table/' + this.table_id).then(response => {
        this.table_no = response.data.table_number
      }).catch(e => {
        this.notifyFail('载入餐桌信息失败')
      })
    }
  }
}
</script>
