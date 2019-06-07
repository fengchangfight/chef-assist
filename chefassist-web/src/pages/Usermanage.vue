<template>
  <q-page style="margin-top:10px;" padding class="  justify-center">
      <div class="row justify-center">
        <q-btn @click="newUser" color="blue" round title="新建用户" icon="add"/>
        <q-btn-group push>
           <q-btn color="blue" @click="filterRole('')" glossy text-color="black" label="所有" icon="verified_user" />
           <q-btn color="blue" @click="filterRole('管理员')" glossy text-color="black" label="管理员" />
           <q-btn color="blue" @click="filterRole('厨师')" glossy text-color="black" label="厨师" />
           <q-btn color="blue" @click="filterRole('服务员')" glossy text-color="black" label="服务员" />
        </q-btn-group>
      </div>
      <div class="row justify-center">
        <q-search v-model="search_name" :debounce="600" placeholder="搜用户" icon="search" float-label="搜用户"/>
      </div>
      <div class="row justify-center" style="margin-top:20px;">
        <q-list highlight inset-separator style="width:300px;">
          <q-item v-for="item in user_items" v-bind:key="item.user_id">
             <q-item-side v-if="item.role_name==='服务员'" avatar="statics/waiter.png" />
             <q-item-side v-if="item.role_name==='厨师'" avatar="statics/cook.png" />
             <q-item-side v-if="item.role_name==='管理员'" avatar="statics/admin.png" />
             <q-item-main style="min-width:120px;" :label="item.username">
               <q-item-tile sublabel>{{item.role_name}}</q-item-tile>
             </q-item-main>
             <!-- <q-item-side right v-if="item.username!=='admin'">
               <q-btn icon="person_pin" dense label="角色" @click="assignRole(item.user_id, item.role_id)" />
               <q-btn icon="settings_backup_restore" dense label="重置密码" @click="passwordReset(item.user_id)" />
               <q-btn icon="delete" label="删除" dense @click="deleteUser(item.user_id, item.username)"/>
             </q-item-side> -->
             <q-item-side right v-if="item.username!=='admin'">
              <q-btn flat round dense icon="more_vert">
              <q-popover>
                <q-list link>
                  <q-item v-close-overlay>
                    <q-item-main label="角色" @click.native="assignRole(item.user_id, item.role_id)" />
                  </q-item>
                  <q-item v-close-overlay>
                    <q-item-main label="重置密码" @click.native="passwordReset(item.user_id)" />
                  </q-item>
                  <q-item v-close-overlay>
                    <q-item-main icon="delete" label="删除" @click.native="deleteUser(item.user_id, item.username)" />
                  </q-item>
                </q-list>
              </q-popover>
            </q-btn>
          </q-item-side>
           </q-item>
         </q-list>
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
    <q-dialog
    v-model="customDialogModel"
    @ok="onOk"
    @cancel="onCancel"
    @show="onShow"
    @hide="onHide"
    >
    <!-- 这里可能使用<q-dialog>的"title"属性 -->
    <span slot="title">新建用户</span>
    <div slot="body">
    <q-field
     icon="account_circle"
     label="用户名"
     :label-width="4"
    >
     <q-input v-model="username" />
    </q-field>
    <q-select
    v-model="role_id"
    float-label="选择角色"
    radio
    :options="role_list"
    />
    </div>

    <template slot="buttons" slot-scope="props">
       <q-btn color="primary" label="创建" @click="executeNewUser(props.ok)" />
    </template>
    </q-dialog>

  </q-page>
</template>

<style>
</style>

<script>
import base from '../mixins/base'
export default {
  name: 'Usermanage',
  mixins: [base],
  data () {
    return {
      search_name: '',
      current_page: 1,
      page_size: 1,
      role_id: '',
      username: '',
      total: 0,
      customDialogModel: false,
      role_list: [],
      user_items: [],
      filter_role_name: '',
      pickRole: ''
    }
  },
  watch: {
    search_name (val) {
      this.loadUsers()
    }
  },
  methods: {
    executeAssignRole (uid, roleId) {
      this.$axios.put('/api/v1/user/assign-role', {
        user_id: uid,
        role_id: roleId
      }).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadUsers()
      }).catch(e => {
        this.notifyFail(e.response.data)
      })
    },
    assignRole (uid, roleId) {
      this.$q.dialog({
        title: '权限角色',
        options: {
          type: 'radio',
          model: '',
          items: this.role_list
        },
        cancel: true,
        preventClose: true,
        color: 'secondary'
      }).then(data => {
        console.log(data)
        if (data == null || data.length < 1) {
          this.$q.notify({
            color: 'orange',
            textColor: 'white',
            icon: 'thumb_up',
            message: '您需要先选中一个角色哦',
            position: 'top-right',
            avatar: 'statics/sad.png'
          })
        } else {
          this.executeAssignRole(uid, data)
        }
      }).catch(() => {})
    },
    passwordReset (userId) {
      this.$axios.put('/api/v1/user/password-reset/' + userId).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    deleteUser (userId, username) {
      this.$q.dialog({
        title: '确认',
        message: '确认删除该用户' + username + '吗？',
        ok: '是',
        cancel: '否'
      }).then(() => {
        this.executeDeleteUser(userId)
      }).catch(() => {
        // do nothing
      })
    },
    executeDeleteUser (userId) {
      this.$axios.delete('/api/v1/user/' + userId).then(response => {
        if (response.data.ok === true) {
          this.notifySuccess(response.data.message)
        } else {
          this.notifyFail(response.data.message)
        }
        this.loadUsers()
      }).catch(e => {
        this.notifyFail(e.response.data.message)
      })
    },
    filterRole (val) {
      this.filter_role_name = val
      this.loadUsers()
    },
    reloadPage (val) {
      this.current_page = val
      this.loadUsers()
    },
    loadUsers () {
      var params = {}
      params.page = this.current_page
      params.filterRoleName = this.filter_role_name
      params.searchName = this.search_name
      this.$axios.get('/api/v1/user', {
        params: params
      }).then(response => {
        this.user_items = response.data.data
        this.total = response.data.total
        this.page_size = response.data.pageSize
        this.current_page = response.data.currentPage
      }).catch(e => {

      })
    },
    executeNewUser (val) {
      if (this.username == null || this.username.length < 1 || this.role_id == null || this.role_id.length < 1) {
        this.$q.notify({
          color: 'red',
          textColor: 'white',
          icon: 'thumb_up',
          message: '用户名和角色不能为空',
          position: 'top-right',
          avatar: 'statics/sad.png'
        })
        return
      }
      this.$axios.post('/api/v1/user/new', {
        username: this.username,
        role_id: this.role_id
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
          this.loadUsers()
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
        this.customDialogModel = false
        this.role_id = ''
        this.username = ''
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
    newUser () {
      this.loadRoles()
      this.customDialogModel = true
    },
    loadRoles () {
      this.$axios.get('/api/v1/role').then(response => {
        this.role_list = []
        for (var i in response.data) {
          var item = {}
          item.label = response.data[i]['role_name']
          item.value = response.data[i]['id']
          this.role_list.push(item)
        }
      }).catch(e => {

      })
    }

  },
  mounted () {
    this.loadUsers()
    this.loadRoles()
  }
}
</script>
