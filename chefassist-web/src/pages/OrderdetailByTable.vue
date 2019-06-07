<template>
  <q-page style="margin-top:10px;" class="row justify-center">
    <div style="max-width: 90vw; margin-top:10px;" >

      <div class="row justify-center">
        <h4>桌号：{{table_no}}</h4>
      </div>
      <div class="row justify-center">
        <q-table
        style="max-width: 90vw;"
        :data="order_items"
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
          <q-btn v-if="props!=null" title="返回餐桌视图" color="purple-7" label="返回" icon="keyboard_return" @click="goPage('/order-by-table')" />
          <q-btn style="margin-left:10px;" v-if="props!=null"  title="分配菜品到厨位" color="teal-6" label="分配到厨位" @click="assignItemsByTable" icon="assignment_returned" />
          <q-btn style="margin-left:10px;" title="结束订单(已买单)" color="positive" label="结束本桌订单" @click="finishOrder" icon="done all" />
          <q-btn style="margin-left:10px;" v-if="props!=null" title="加菜" label="加菜" color="blue" @click="goToOrderPage" icon="add" />
          <q-btn style="margin-left:10px;" v-if="props!=null" title="刷新列表" round color="blue" @click="refreshItems" icon="refresh" />
        </template>
        <q-td slot="body-cell-Status" slot-scope="props" :props="props">
          <q-chip v-if="props.value==='unassigned'" small color="brown-4">未分配</q-chip>
          <q-chip v-else-if="props.value==='waiting_assign'" small color="yellow">待分配</q-chip>
          <q-chip v-else-if="props.value==='failed_assign'" small color="red">分配失败</q-chip>
          <q-chip v-else-if="props.value==='assigned'" small color="lime-14">制作中</q-chip>
          <q-chip v-else-if="props.value==='readytoserve'" small color="deep-orange-6">完成待取</q-chip>
          <q-chip v-else-if="props.value==='served'" small color="light-green-10">已上菜</q-chip>
          <q-chip v-else small color="green">props.value</q-chip>
        </q-td>
        <q-td slot="body-cell-OrderNo" slot-scope="props" :props="props">
          <a href="javascript:" @click.prevent="goPage('/order-detail/'+props.row.order_id)">{{props.value}}</a>
        </q-td>
        <q-td style="min-width:150px;" slot="body-cell-DishCount" slot-scope="props" :props="props">
          <q-btn :disable="props.row.status!=='unassigned'" size="sm" round dense color="secondary" icon="remove" @click="updateUnassignedItemCount(props.row.id, props.value-1)" class="q-mr-xs" />
          <q-btn :disable="props.row.status!=='unassigned'" size="sm" round dense color="tertiary" icon="add" @click="updateUnassignedItemCount(props.row.id, props.value+1)" class="q-mr-sm" />
          {{props.value}}
        </q-td>
        <q-td style="max-width:300px;" slot="body-cell-Description" slot-scope="props" :props="props">
          <q-btn v-if="props.row.status!=='served'" @click="openEditDescriptionDialog(props.row.id,props.value)" size="sm" class="q-mr-sm" icon="mode edit" dense></q-btn>
          <span>{{props.value}}</span>
        </q-td>
        <q-td style="max-width:150px;min-width:100px;" slot="body-cell-Action" slot-scope="props" :props="props">
          <q-btn v-if="props.row.status==='readytoserve'" @click="served(props.row.id)" icon="speaker_notes_off" dense glossy color="purple-13" label="划菜" />
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
  name: 'OrderdetailByTable',
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
          name: 'OrderNo',
          required: true,
          label: '订单号',
          align: 'left',
          field: 'order_number',
          sortable: false
        },
        {
          name: 'DishName',
          required: true,
          label: '菜名',
          align: 'left',
          field: 'dish_name',
          sortable: true
        }, {
          name: 'Status',
          required: true,
          label: '状态',
          align: 'left',
          field: 'status',
          sortable: true
        }, {
          name: 'Description',
          required: true,
          label: '备注',
          align: 'left',
          field: 'description',
          sortable: true
        }, {
          name: 'DishCount',
          required: true,
          label: '数量',
          align: 'left',
          field: 'dish_count',
          sortable: true
        }, {
          name: 'ProducerNumber',
          required: true,
          label: '分配厨位',
          align: 'left',
          field: 'producer_number',
          sortable: true
        },
        {
          name: 'Action',
          required: true,
          label: '操作',
          align: 'left',
          sortable: false
        },
        {
          name: 'LastUpdateTime',
          required: true,
          label: '更新时间',
          align: 'left',
          field: 'last_update_time',
          sortable: true
        }
        // ,
        // {
        //   name: 'Operations',
        //   required: true,
        //   label: '操作',
        //   align: 'left'
        // }
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
      order_items: []
    }
  },
  mixins: [base],
  mounted () {
    this.table_id = this.$route.params.id
    this.loadTableDetail(this.table_id)
    this.loadOrderItemsByTableId(this.table_id, this.table_page_size)
  },
  methods: {
    newOrder () {
      this.$axios.post('/api/v1/order/new').then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
          this.goPage('/order-detail/' + response.data.data)
        }
      }).catch(e => {

      })
    },
    finishOrder () {
      this.$axios.put('/api/v1/table/finish/' + this.table_id).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.refreshItems()
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    goToOrderPage () {
      if (this.order_items == null || this.order_items.length < 1) {
        this.newOrder()
        return
      }
      var activeOrderId = this.order_items[0].order_id
      this.goPage('/order-detail/' + activeOrderId)
    },
    assignItemsByTable () {
      if (this.order_items == null || this.order_items.length < 1) {
        this.notifyWarn('无可分配菜品')
        return
      }
      var activeOrderId = this.order_items[0].order_id

      this.$axios.post('/api/v1/assignment/' + activeOrderId).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
          setTimeout(() => {
            this.refreshItems()
          }, 1000)
        } else {
          this.notifyFail(response.data.message)
        }
      }).catch(e => {
        this.notifyFail('未知错误')
      })
    },
    served (itemId) {
      this.$axios.put('/api/v1/order-item/served/' + itemId).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.refreshItems()
      }).catch(e => {
        this.notifyFail('未知错误served')
      })
    },
    openEditDescriptionDialog (itemId, value) {
      this.$q.dialog({
        title: '修改备注',
        prompt: {
          model: value,
          type: 'text' // optional
        },
        cancel: true,
        color: 'secondary'
      }).then(data => {
        this.updateItemDescription(itemId, data)
      }).catch(() => {
        //   this.$q.notify('Ok, no mood for talking, right?')
      })
    },
    updateItemDescription (itemId, newDescription) {
      this.$axios.put('/api/v1/order-item/description', {
        item_id: itemId,
        description: newDescription
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
          this.refreshItems()
        } else {
          this.notifyFail(response.data.message)
        }
      }).catch(e => {
        this.notifyFail('未知错误updateItemDescription')
      })
    },
    updateUnassignedItemCount (itemId, count) {
      if (count < 0) {
        count = 0
      }
      this.$axios.put('/api/v1/order-item/unassigned-count', {
        item_id: itemId,
        count: count
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
          this.refreshItems()
        } else {
          this.notifyFail(response.data.message)
        }
      }).catch(e => {
        this.notifyFail('未知错误od1')
      })
    },
    refreshItems () {
      this.loadOrderItemsByTableId(this.table_id, this.table_page_size)
    },
    request (props) {
      console.log(props)
      this.serverPagination = props.pagination
      this.loadOrderItemsByTableId(this.table_id, this.table_page_size)
    },
    changeCount (val) {
      this.count = this.count + val
      if (this.count < 0) {
        this.count = 0
      }
    },
    reloadPage (val) {
      this.serverPagination.page = val
      this.loadOrderItemsByTableId(this.table_id, this.table_page_size)
    },
    loadOrderItemsByTableId (tableId, pageSize) {
      this.loading = true
      var params = {}
      params.page = this.serverPagination.page
      params.tableId = tableId
      params.pageSize = pageSize
      this.$axios.get('/api/v1/order-item/active-item-of-table', {
        params: params
      }).then(response => {
        this.order_items = response.data.data
        this.serverPagination.page = response.data.currentPage
        this.serverPagination.rowsNumber = response.data.total
        this.serverPagination.rowsPerPage = response.data.pageSize
        this.loading = false
      }).catch(e => {
        console.log('===failed loading order items')
      })
    },
    loadTableDetail (tableId) {
      this.$axios.get('/api/v1/table/' + tableId).then(response => {
        var table = response.data
        console.log(table)
        this.table_no = table.table_number
      }).catch(e => {
        this.notifyFail('未知错误ob')
      })
    }
  }
}
</script>
