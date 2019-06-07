<template>
  <q-page class="flex flex-center">
    <div style="width: 500px; max-width: 90vw;">
          <q-input v-model="username" float-label="用户名" @keyup.enter="login"/>
          <q-input v-model="password" type="password" float-label="密码" @keyup.enter="login" />
          <q-btn @click="login" color="primary" class="full-width" label="登录" />
    </div>
  </q-page>
</template>

<style>
.q-btn {
    margin: 5px;
}
</style>

<script>
import Qs from 'qs'
export default {
  name: 'PageLogin',
  data () {
    return {
      username: '',
      password: ''
    }
  },
  methods: {
    login () {
      var data = { 'username': this.username, 'password': this.password, 'timeout': 1000 }
      this.$axios.post('/login', Qs.stringify(data), { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then(response => {
        // location.reload();
        window.location.href = '/'
        // this.$router.push('/working');
      }).catch(e => {
        this.$q.notify({
          color: 'red',
          textColor: 'white',
          icon: 'thumb_up',
          message: '用户名或密码不对',
          position: 'top-right',
          avatar: 'statics/sad.png'
        })
      })
    }
  }
}
</script>
