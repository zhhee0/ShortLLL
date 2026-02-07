import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import {
  ArrowLeft,
  CaretBottom,
  Delete,
  HelpFilled,
  Histogram,
  Iphone,
  Link,
  Message,
  Share,
  Tickets,
  Tools,
  User
} from '@element-plus/icons-vue'
import './style.scss'
import API from '@/api/index'
import store from './store'
// import ViewUIPlus from 'view-ui-plus'
// import 'view-ui-plus/dist/styles/viewuiplus.css'


const app = createApp(App)

app.config.globalProperties.$API = API
// 屏蔽警告信息
app.config.warnHandler = () => null;
app.use(router)
app.use(ElementPlus)
app.use(store)
// app.use(ViewUIPlus)
const iconComponents = [
  ArrowLeft,
  CaretBottom,
  Delete,
  HelpFilled,
  Histogram,
  Iphone,
  Link,
  Message,
  Share,
  Tickets,
  Tools,
  User
]
iconComponents.forEach((icon) => {
  app.component(icon.name, icon)
})

app.mount('#app')
