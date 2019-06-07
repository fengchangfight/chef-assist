<template>
  <q-page style="margin-top:10px;" class="row justify-center" >
    <div style="max-width: 90vw;" >
      <div class="row justify-center">
        <h4>餐桌管理</h4>
      </div>
      <div class="row justify-center">
        <q-btn @click="newTable" color="blue" label="新建餐桌" title="新建餐桌" icon="playlist_add"/>
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
            <q-btn @click="deleteTable(table.id)" flat>删除</q-btn>
            <q-btn @click="editTable(table.id)" flat>编辑</q-btn>
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
    <q-dialog
    v-model="newTableDialog"
    @ok="onOk"
    @cancel="onCancel"
    @show="onShow"
    @hide="onHide"
    >
    <!-- 这里可能使用<q-dialog>的"title"属性 -->
    <span slot="title">新建餐桌</span>
    <div slot="body">
    <q-field
     icon="settings_input_composite"
     label="桌号(必填)"
     :label-width="5"
    >
     <q-input v-model="k_table_no" />
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
       <q-btn color="primary" label="创建" @click="executeNewTable(props.ok)" />
    </template>
    </q-dialog>
    <q-dialog
    v-model="editTableDialog"
    @ok="onOk"
    @cancel="onCancel"
    @show="onShow"
    @hide="onHide"
    >
    <!-- 这里可能使用<q-dialog>的"title"属性 -->
    <span slot="title">编辑餐桌</span>
    <div slot="body">
    <q-field
     icon="settings_input_composite"
     label="桌号(必填)"
     :label-width="5"
    >
     <q-input v-model="k_table_no" />
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
       <q-btn color="primary" label="更新" @click="executeEditTable(props.ok)" />
    </template>
    </q-dialog>
  </q-page>
</template>

<style>
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'Tablemanage',
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
    editTable (id) {
      // load table info by id
      this.$axios.get('/api/v1/table/' + id).then(response => {
        this.k_table_no = response.data.table_number
        this.k_description = response.data.description
        this.edit_table_id = response.data.id
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })

      this.editTableDialog = true
    },
    deleteTable (id) {
      this.$q.dialog({
        title: '确认',
        message: '确认删除该餐桌吗？',
        ok: '是',
        cancel: '否'
      }).then(() => {
        this.$axios.delete('/api/v1/table/' + id).then(response => {
          if (response.data.ok === true) {
            this.notifySuccess(response.data.message)
          } else {
            this.notifyFail(response.data.message)
          }
          this.loadTables()
        }).catch(e => {
          this.notifyFail(e.response.data.message)
        })
      }).catch(() => {
        // do nothing
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
    },
    newTable () {
      this.newTableDialog = true
    },
    executeEditTable () {
      if (this.checkStringNull(this.k_table_no)) {
        this.notifyWarn('桌号不能为空')
        return
      }
      this.$axios.put('/api/v1/table/' + this.edit_table_id, {
        table_number: this.k_table_no,
        description: this.k_description
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadTables()
        this.editTableDialog = false
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    executeNewTable () {
      this.$axios.post('/api/v1/table', {
        table_number: this.k_table_no,
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
          this.newTableDialog = false
          this.loadTables()
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
    }
  }
}
</script>
