<template>
  <div class="common-layout">
    <el-container>
      <el-header height="54px" style="padding: 0">
        <div class="header">
          <div @click="toMySpace" class="logo">增强shortLinkPlatform</div>
          <div class="header-note">
            <div>
              京东云送了15天,借此部署上线,代码开源在
              <a
                class="note-link"
                target="_blank"
                rel="noopener"
                href="https://github.com/zhhee0/ShortLLL"
                >github</a
              >,欢迎star
            </div>
            <div class="header-clock">{{ clockText }}</div>
          </div>
          <div class="header-actions">
            <a
              class="link-span"
              style="text-decoration: none"
              target="_blank"
              href="https://space.bilibili.com/546047204?spm_id_from=333.1007.0.0"
              >🔥作者主页</a
            >
            <RouterLink
              class="link-span"
              style="text-decoration: none"
              to="/home/story"
              >🔥故事会(娱乐)🔥</RouterLink
            >
            <a
                class="link-span"
                style="text-decoration: none"
                target="_blank"
                href="https://codetop.cc/home"
            >🔥小学奥数</a
            >
            <a
                class="link-span"
                style="text-decoration: none"
                target="_blank"
                href="https://www.xiaolincoding.com/"
            >🔥清朝八股文</a
            >
            <el-dropdown>
              <div class="block">
                <span
                    class="name-span"
                    style="text-decoration: none"
                >{{username}}</span
                >
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toMine">个人信息</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main style="padding: 0">
        <div class="content-box">
          <RouterView class="content-space" />
        </div>
      </el-main>
      <!-- <el-container>
        <el-aside width="180px">
          <el-menu
            active-text-color="#073372"
            background-color="#0e5782"
            class="el-menu-vertical-demo"
            :default-active="getLasteRoute(route.path)"
            text-color="#fff"
            @select="handleSelect"
          >
            <template v-for="item in menuInfos" :key="item.name">
              <el-menu-item :index="item.path">
                <el-icon><icon-menu /></el-icon>
                <span>{{ item.name }}</span>
              </el-menu-item>
            </template>
          </el-menu></el-aside
        >

      </el-container> -->
    </el-container>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { removeKey, removeUsername, getToken, getUsername } from '@/core/auth.js'
import { ElMessage } from 'element-plus'
const { proxy } = getCurrentInstance()
const API = proxy.$API
// 当当前路径和菜单不匹配时，菜单不会被选中
const router = useRouter()
const squareUrl = ref('https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png')
const clockText = ref('')
// Use a fixed site launch time so refreshes/user actions don't change uptime.
const launchTime = ref(new Date(2026, 1, 5, 21, 25, 0).getTime())
const expireTime = new Date(2026, 1, 19, 0, 0, 0)
let clockTimer = null

const formatDuration = (ms) => {
  const safeMs = ms < 0 ? 0 : ms
  const totalSeconds = Math.floor(safeMs / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  return `${days}天${hours}时${minutes}分${seconds}秒`
}

const updateClock = () => {
  if (!launchTime.value) {
    return
  }
  const now = new Date()
  const runMs = now.getTime() - launchTime.value
  const leftMs = expireTime - now
  clockText.value = `网站已运行${formatDuration(runMs)} 距离网站死亡还剩 ${formatDuration(leftMs)}`
}
const toMine = () => {
  router.push('/home' + '/account')
}
// 登出
const logout = async () => {
  const token = getToken()
  const username = getUsername()
  // 请求登出的接口
  await API.user.logout({ token, username })
  // 删除cookies中的token和username
  removeUsername()
  removeKey()
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
  ElMessage.success('成功退出！')
}
// 点击左上方的图片跳转到我的空间
const toMySpace = () => {
  router.push('/home' + '/space')
}
const username = ref('')
onMounted(async () => {
  const actualUsername = getUsername()
  const res = await API.user.queryUserInfo(actualUsername)
  // firstName.value = res?.data?.data?.realName?.split('')[0]
  username.value = truncateText(actualUsername, 8)

  updateClock()
  clockTimer = setInterval(updateClock, 1000)
})

onUnmounted(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
    clockTimer = null
  }
})
const extractColorByName = (name) => {
  var temp = []
  temp.push('#')
  for (let index = 0; index < name.length; index++) {
    temp.push(parseInt(name[index].charCodeAt(0), 10).toString(16))
  }
  return temp.slice(0, 5).join('').slice(0, 4)
}

// 辅助函数，用于截断文本
const truncateText = (text, maxLength) => {
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}
</script>

<style lang="scss" scoped>
.el-container {
  height: 100vh;

  .el-aside {
    border: 0;
    background-color: #0e5782;

    ul {
      border: 0px;
    }
  }

  .el-main {
    background-color: #e8e8e8;
  }
}

.header {
  color: #e6f7ff;
  background:
    radial-gradient(120% 160% at 8% 0%, rgba(120, 220, 255, 0.3), transparent 62%),
    radial-gradient(140% 160% at 100% 0%, rgba(80, 150, 255, 0.26), transparent 58%),
    linear-gradient(92deg, rgba(10, 30, 55, 0.52) 0%, rgba(8, 60, 95, 0.34) 55%, rgba(10, 36, 60, 0.28) 100%);
  background-color: rgba(8, 26, 45, 0.28);
  border: 1px solid rgba(130, 210, 255, 0.26);
  backdrop-filter: blur(18px) saturate(170%);
  -webkit-backdrop-filter: blur(18px) saturate(170%);
  box-shadow: 0 12px 30px rgba(2, 10, 20, 0.32);
  padding: 0 0 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .block {
    cursor: pointer;
    display: flex;
    align-items: center;
    border: 0px;
  }
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-note {
  flex: 1;
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.86);
  font-family: 'Helvetica Neue', Helvetica, STHeiTi, Arial, sans-serif;
  padding: 0 16px;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  line-height: 1.3;
}

.header-clock {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.note-link {
  color: #ffffff;
  text-decoration: underline;
}

.note-link:hover {
  color: #ffffff;
  text-decoration: none;
}

.content-box {
  height: calc(100vh - 50px);
  background-color: white;
}

:deep(.el-tooltip__trigger:focus-visible) {
  outline: unset;
}

.logo {
  font-size: 15px;
  font-weight: 600;
  color: #e8e8e8;
  font-family: Helvetica, Tahoma, Arial, 'PingFang SC', 'Hiragino Sans GB', 'Heiti SC',
    'Microsoft YaHei', 'WenQuanYi Micro Hei';
  // font-family: 'Helvetica Neue', Helvetica, STHeiTi, Arial, sans-serif;
  cursor: pointer;
}

.logo:hover {
  color: #fff;
}

.link-span {
  color: #fff;
  opacity: .6;
  margin-right: 30px;
  font-size: 16px;
  font-family: 'Helvetica Neue', Helvetica, STHeiTi, Arial, sans-serif;
  cursor: pointer;
  text-decoration: none;
}

.link-span:hover {
  text-decoration: underline !important;
  opacity: 1;
  color: #fff;
}

.name-span {
  color: #fff;
  opacity: .6;
  margin-right: 30px;
  font-size: 12px;
  font-family: 'Helvetica Neue', Helvetica, STHeiTi, Arial, sans-serif;
  cursor: pointer;
  text-decoration: none;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

@media (max-width: 768px) {
  .el-header {
    height: auto !important;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    padding: 8px 12px;
    gap: 8px;
  }

  .header-note {
    width: 100%;
    text-align: left;
    padding: 0;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
    gap: 6px 10px;
  }

  .link-span {
    margin-right: 12px;
    font-size: 12px;
  }

  .name-span {
    margin-right: 12px;
  }

  .content-box {
    height: auto;
    min-height: calc(100vh - 54px);
  }
}

.avatar {
  transform: translateY(-2px);
}
</style>
