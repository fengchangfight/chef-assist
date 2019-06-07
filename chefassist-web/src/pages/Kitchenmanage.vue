<template>
  <q-page  style="margin-top:10px;" class="row justify-center"  >
    <div style="width: 700px; max-width: 90vw;" >
      <div class="row justify-center">
        <h4>厨房管理</h4>
      </div>
      <div class="row justify-center">
        <q-btn @click="newPosition" color="blue" title="新建厨位" label="新建厨位" icon="playlist_add"/>
      </div>
      <div class="row justify-center">
        <q-card v-bind:class="{'offline-card': position.status==='offline', 'online-card': position.status==='online', 'blocking-card': position.status==='blocking' }" v-for="position in positions" v-bind:key="position.id" inline class="q-ma-sm">
          <q-card-media>
            <img src="statics/chuwei.png">
            <q-card-title slot="overlay">
             {{position.producer_number}}
             <span slot="subtitle">{{position.description}}</span>
            </q-card-title>
          </q-card-media>
          <q-card-actions>
            <q-btn @click="deletePosition(position.id)" flat>删除</q-btn>
            <q-btn @click="editPosition(position.id)" flat>编辑</q-btn>
          </q-card-actions>
         <q-item>
           <q-item-main>
             <q-btn-toggle
            @input="toggleStatus(position.status,position.id)"
            v-model="position.status"
            push
            glossy
            toggle-color="primary"
            :options="[
              {label: '上线', value: 'online'},
              {label: '阻挡', value: 'blocking'},
              {label: '离线', value: 'offline'}
            ]"
           />
           </q-item-main>
        </q-item>
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
    <q-dialog
    v-model="customDialogModel"
    @ok="onOk"
    @cancel="onCancel"
    @show="onShow"
    @hide="onHide"
    >
    <!-- 这里可能使用<q-dialog>的"title"属性 -->
    <span slot="title">新建厨位</span>
    <div slot="body">
    <q-field
     icon="settings_input_composite"
     label="厨位号(必填)"
     :label-width="5"
    >
     <q-input v-model="k_position_no" />
    </q-field>
    <q-field
     icon="description"
     label="描述"
     :label-width="4"
    >
     <q-input v-model="k_description" />
    </q-field>
    </div>

    <template slot="buttons" slot-scope="props">
       <q-btn color="primary" label="创建" @click="executeNewPosition(props.ok)" />
    </template>
    </q-dialog>

    <q-dialog
    v-model="editDishProducerDialog"
    @ok="onOk"
    @cancel="onCancel"
    @show="onShow"
    @hide="onHide"
    >
    <!-- 这里可能使用<q-dialog>的"title"属性 -->
    <span slot="title">编辑厨位</span>
    <div slot="body">
    <q-field
     icon="settings_input_composite"
     label="桌号(必填)"
     :label-width="5"
    >
     <q-input v-model="k_dish_producer_no" />
    </q-field>
    <q-field
     icon="description"
     label="描述"
     :label-width="4"
    >
    <q-input v-model="k_dish_producer_description" />
    </q-field>
    </div>
    <template slot="buttons" slot-scope="props">
       <q-btn color="primary" label="更新" @click="executeEditDishProducer(props.ok)" />
    </template>
    </q-dialog>
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
  name: 'Kitchenmanage',
  data () {
    return {
      k_dish_producer_no: '',
      k_dish_producer_description: '',
      k_dish_producer_id: '',
      editDishProducerDialog: false,
      selected: {},
      current_page: 1,
      k_description: '',
      k_position_no: '',
      customDialogModel: false,
      positions: [],
      total: 0,
      page_size: 1
    }
  },
  mixins: [base],
  mounted () {
    this.loadKPositions()
  },
  methods: {
    toggleStatus (status, id) {
      this.$axios.put('/api/v1/dish-producer/status/' + id, {
        status: status
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadKPositions()
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    reloadPage (val) {
      this.current_page = val
      this.loadKPositions()
    },
    executeEditDishProducer () {
      if (this.checkStringNull(this.k_dish_producer_no)) {
        this.notifyWarn('厨位号不能为空')
        return
      }
      this.$axios.put('/api/v1/dish-producer/' + this.k_dish_producer_id, {
        producer_number: this.k_dish_producer_no,
        description: this.k_dish_producer_description
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadKPositions()
        this.editDishProducerDialog = false
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    editPosition (id) {
      this.$axios.get('/api/v1/dish-producer/' + id).then(response => {
        this.k_dish_producer_no = response.data.producer_number
        this.k_dish_producer_description = response.data.description
        this.k_dish_producer_id = response.data.id
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
      this.editDishProducerDialog = true
    },
    deletePosition (id) {
      this.$q.dialog({
        title: '删除确认',
        ok: '是',
        cancel: '否'
      }).then(() => {
        this.$axios.delete('/api/v1/dish-producer/' + id).then(response => {
          if (response.data.ok === true) {
            this.notifySuccess(response.data.message)
          } else {
            this.notifyFail(response.data.message)
          }
          this.loadKPositions()
        }).catch(e => {
          this.notifyFail(e.response.data.message)
        })
      }).catch(() => {

      })
    },
    loadKPositions () {
      var params = {}
      params.page = this.current_page
      this.$axios.get('/api/v1/dish-producer', {
        params: params
      }).then(response => {
        this.positions = response.data.data
        this.current_page = response.data.currentPage
        this.page_size = response.data.pageSize
        this.total = response.data.total
      }).catch(e => {

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
    executeNewPosition () {
      this.$axios.post('/api/v1/dish-producer', {
        producer_number: this.k_position_no,
        description: this.k_description
      }).then(response => {
        if (response.data.ok === true) {
          this.$q.notify({
            color: 'green',
            textColor: 'white',
            icon: 'thumb_up',
            message: response.data.message,
            position: 'top-right',
            avatar: 'statics/huaji.png'
          })
          this.customDialogModel = false
          this.loadKPositions()
        } else {
          this.$q.notify({
            color: 'red',
            textColor: 'white',
            icon: 'thumb_up',
            message: response.data.message,
            position: 'top-right',
            avatar: 'statics/sad.png'
          })
        }
      }).catch(e => {
        this.$q.notify({
          color: 'red',
          textColor: 'white',
          icon: 'thumb_up',
          message: '未知错误',
          position: 'top-right',
          avatar: 'statics/sad.png'
        })
      })
    },
    newPosition () {
      this.customDialogModel = true
    }
  }
}
</script>
