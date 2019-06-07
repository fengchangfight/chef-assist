import axios from 'axios'

export default ({ Vue }) => {
  Vue.prototype.$axios = axios.create({
    // baseURL: 'http://192.168.3.8:8686',
    baseURL: 'http://localhost:8686',
    withCredentials: true
    // timeout: 1000,
    // headers: {
    //   'header': 'value'
    // }
  })
}
