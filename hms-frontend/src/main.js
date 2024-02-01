import {createApp} from 'vue'
import App from './App.vue'
import router from "@/router";
import axios from "axios";
import 'element-plus/theme-chalk/dark/css-vars.css';
import {ElementPlus} from "@element-plus/icons-vue";

// 后端提交地址
axios.defaults.baseURL = 'http://localhost:8080'

const app = createApp(App)

app.use(router)

app.mount('#app')
