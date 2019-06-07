<template>
  <q-page style="margin-top:10px;" class="row justify-center">
    <div style="max-width: 90vw; margin-top:10px;" >
      <p class="caption">订单号：{{order_no}}</p>
      <p class="caption">创建人: {{created_by}}</p>
      <div class="row justify-center" style="margin-top: 10px;">
        <q-table
        style="max-width: 90vw;"
        :data="order_items"
        :columns="columns"
        row-key="id"
        color="secondary"
        :loading="loading"
        >
        <template slot="top-right" slot-scope="props">
          <span v-if="props!=null" style="margin-left: 10px;"><q-chip color="blue-grey-6">总价:{{totalPrice}}￥</q-chip></span>
        </template>
        <template slot="top-left" slot-scope="props">
          <q-btn v-if="props!=null && order_status!=='finished'" title="返回进行中订单列表"  color="purple-7" icon="keyboard_return" label="返回" @click="goPage('/order-manage')" />
          <q-btn v-if="props!=null && order_status==='finished'" title="返回历史订单列表"  color="purple-7" icon="keyboard_return" label="返回" @click="goPage('/report-manage')" />
          <q-btn :disabled="order_status==='finished'" style="margin-left:10px;" v-if="props!=null"  title="分配菜品到厨位" color="teal-6" label="分配到厨位" @click="assignItems" icon="assignment_returned" />
          <q-btn style="margin-left:10px;" v-if="props!=null && order_status!=='finished'"  title="结束订单(已买单)" color="positive" label="结束订单" @click="finishOrder" icon="done all" />
          <q-btn style="margin-left:10px;" v-if="props!=null && order_status!=='finished'"  title="加菜" color="blue" label="加菜" @click="toAddDishPage" icon="add" />
          <q-chip color="blue-grey-13" v-if="props!=null && order_status==='finished'" style="margin-left:10px;">已完成</q-chip>
          <q-btn v-if="props!=null" style="margin-left:10px;" title="刷新列表" round color="blue" @click="refreshItems" icon="refresh" />
        </template>
        <q-td style="min-width:115px;" slot="body-cell-Status" slot-scope="props" :props="props">
          <q-chip v-if="props.value==='unassigned'" small color="brown-4">未分配</q-chip>
          <q-chip v-else-if="props.value==='waiting_assign'" small color="yellow">待分配</q-chip>
          <q-chip v-else-if="props.value==='failed_assign'" small color="red">分配失败</q-chip>
          <q-chip v-else-if="props.value==='assigned'" small color="lime-14">制作中</q-chip>
          <q-chip v-else-if="props.value==='readytoserve'" small color="deep-orange-6">完成待取</q-chip>
          <q-chip v-else-if="props.value==='served'" small color="green">已上菜</q-chip>
          <q-chip v-else small color="green">props.value</q-chip>
        </q-td>
        <q-td style="min-width:150px;" slot="body-cell-DishCount" slot-scope="props" :props="props">
          <q-btn :disable="props.row.status!=='unassigned'" size="sm" round dense color="secondary" icon="remove" @click="updateUnassignedItemCount(props.row.id, props.value-1)" class="q-mr-xs" />
          <q-btn :disable="props.row.status!=='unassigned'" size="sm" round dense color="tertiary" icon="add" @click="updateUnassignedItemCount(props.row.id, props.value+1)" class="q-mr-sm" />
          {{props.value}}
        </q-td>
        <q-td style="max-width:120px;" slot="body-cell-Price" slot-scope="props" :props="props">
           <q-chip>{{props.value}}￥</q-chip>
        </q-td>
        <q-td style="max-width:300px;" slot="body-cell-Description" slot-scope="props" :props="props">
          <q-btn v-if="props.row.status!=='served'" @click="openEditDescriptionDialog(props.row.id,props.value)" size="sm" class="q-mr-sm" icon="mode edit" dense></q-btn>
          <span>{{props.value}}</span>
        </q-td>
        <q-td style="max-width:150px;" slot="body-cell-Action" slot-scope="props" :props="props">
          <q-btn v-if="props.row.status==='readytoserve'" @click="served(props.row.id)" icon="speaker_notes_off" dense glossy color="purple-13" label="划菜" />
        </q-td>
        <q-td style="width:100px;max-width:200px;" slot="body-cell-TableNumber" slot-scope="props" :props="props">
          <a @click.prevent="goPage('/order-detail-by-table/'+props.row.table_id)" href="javascript:">{{props.value}}</a>
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
  name: 'Orderdetail',
  data () {
    return {
      loading: false,
      columns: [
        {
          name: 'DishName',
          required: true,
          label: '菜名',
          align: 'left',
          field: 'dish_name',
          sortable: false
        }, {
          name: 'Status',
          required: true,
          label: '状态',
          align: 'left',
          field: 'status',
          sortable: false
        },
        {
          name: 'TableNumber',
          required: true,
          label: '桌号',
          align: 'left',
          field: 'table_number',
          sortable: false
        }, {
          name: 'Description',
          required: false,
          label: '备注',
          align: 'left',
          field: 'description',
          sortable: false
        }, {
          name: 'DishCount',
          required: true,
          label: '数量',
          align: 'left',
          field: 'dish_count',
          sortable: false
        }, {
          name: 'Price',
          required: true,
          label: '单价',
          align: 'left',
          field: 'price',
          sortable: false
        },
        {
          name: 'ProducerNumber',
          required: true,
          label: '分配厨位',
          align: 'left',
          field: 'producer_number',
          sortable: false
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
          sortable: false
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
      order_status: '',
      page_size: 1,
      description: '',
      table_page_size: 100,
      order_id: '',
      order_no: '',
      created_by: '',
      order_items: []
    }
  },
  mixins: [base],
  computed: {
    totalPrice: function () {
      var tPrice = 0.0

      console.log(this.order_items)
      for (var i in this.order_items) {
        console.log(this.order_items[i].price)
        tPrice = tPrice + this.order_items[i].price * this.order_items[i].dish_count
      }
      return tPrice
    }
  },
  mounted () {
    this.order_id = this.$route.params.id
    this.loadOrderDetail(this.order_id)

    this.loadOrderItemsByOrderId(this.order_id, this.table_page_size)

    var storeThis = this
    setInterval(function () {
      storeThis.refreshItems()
      console.log('refresh order detail items list')
    }, 20000)
  },
  methods: {
    toAddDishPage () {
      this.goPage('/add-dish-item-4-order/' + this.order_id)
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
    finishOrder () {
      this.$axios.put('/api/v1/order/finish/' + this.order_id).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadOrderDetail(this.order_id)
      }).catch(e => {
        this.notifyFail(e.response.data.message)
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
    assignItems () {
      this.$axios.post('/api/v1/assignment/' + this.order_id).then(response => {
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
    refreshItems () {
      this.loadOrderItemsByOrderId(this.order_id, this.table_page_size)
    },
    request (props) {
      console.log(props)
      this.serverPagination = props.pagination
      this.loadOrderItemsByOrderId(this.order_id, this.table_page_size)
    },
    reloadPage (val) {
      this.serverPagination.page = val
      this.loadOrderItemsByOrderId(this.order_id, this.table_page_size)
    },
    loadOrderItemsByOrderId (orderId, pageSize) {
      this.loading = true
      var params = {}
      params.orderId = orderId
      this.$axios.get('/api/v1/order-item', {
        params: params
      }).then(response => {
        this.order_items = response.data
        this.loading = false
      }).catch(e => {
        console.log('===failed loading order items')
      })
    },

    loadOrderDetail (orderId) {
      this.$axios.get('/api/v1/order/' + orderId).then(response => {
        var order = response.data
        this.order_no = order.order_number
        this.created_by = order.created_by
        this.order_status = order.order_status
      }).catch(e => {
        this.notifyFail('未知错误ob')
      })
    }
  }
}
</script>
