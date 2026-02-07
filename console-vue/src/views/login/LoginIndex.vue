<template>
  <div class="login-page" :class="{ 'is-register': !isLogin }">
    <div class="bg-aurora"></div>
    <div class="bg-grid"></div>
    <div class="bg-beam"></div>
    <div ref="vantaRef" class="vanta"></div>
    <header class="login-header">
      <div class="brand">
        <div class="brand-mark">SL</div>
        <div class="brand-copy">
          <span class="brand-name">ShortLink Studio</span>
          <span class="brand-sub">SaaS 短链增长工作台</span>
        </div>
      </div>
      <div class="header-actions">
      </div>
    </header>
    <main class="login-shell">
      <section class="hero-panel">
        <div class="hero-badge">可观测 · 可增长 · 可扩展</div>
        <h1>把每一次点击，变成可复盘的增长线索</h1>
        <p>
          用户可以注册，也可以用默认账号登录，多租户数据隔离：每条短链、分组、统计记录都带上用户标识
        </p>
        <div class="hero-metrics">
          <div class="metric-card">
            <div class="metric-value">高并发</div>
            <div class="metric-label">峰值流量稳定承载</div>
          </div>
          <div class="metric-card">
            <div class="metric-value">高可用</div>
            <div class="metric-label">核心链路持续可用</div>
          </div>
          <div class="metric-card">
            <div class="metric-value">流量削峰</div>
            <div class="metric-label">平滑突发流量波动</div>
          </div>
        </div>
        <div class="hero-footnote">
          轻量化部署，支持私有化和多租户，适合营销、运营、产品和增长团队协作。
        </div>
      </section>
      <section class="auth-panel">
        <div class="auth-card">
          <div class="auth-switch">
            <button type="button" :class="{ active: isLogin }" @click="changeLogin(true)">登录</button>
            <button type="button" :class="{ active: !isLogin }" @click="changeLogin(false)">注册</button>
          </div>
          <div class="auth-title">
            <h2>{{ isLogin ? '欢迎回来' : '开始创建账号' }}</h2>
            <p>{{ isLogin ? '使用账户进入短链控制台。' : '填写信息，建立你的工作空间。' }}</p>
          </div>
          <div class="auth-body">
            <el-form
              v-show="isLogin"
              ref="loginFormRef1"
              :model="loginForm"
              :rules="loginFormRule"
              label-position="top"
              class="auth-form"
            >
              <el-form-item prop="phone" label="用户名">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" maxlength="11" show-word-limit clearable />
              </el-form-item>
              <el-form-item prop="password" label="密码">
                <el-input v-model="loginForm.password" type="password" clearable placeholder="请输入密码" show-password />
              </el-form-item>
              <div class="form-row">
                <el-checkbox class="remeber-password" v-model="checked">记住密码</el-checkbox>
                <span class="helper-text">建议仅在个人设备上勾选</span>
              </div>
              <el-button
                :loading="loading"
                @keyup.enter="login"
                type="primary"
                class="primary-btn"
                @click="login(loginFormRef1)"
              >
                进入控制台
              </el-button>
            </el-form>
            <el-form
              v-show="!isLogin"
              ref="loginFormRef2"
              :model="addForm"
              :rules="addFormRule"
              label-position="top"
              class="auth-form"
            >
              <el-form-item prop="username" label="用户名">
                <el-input v-model="addForm.username" placeholder="请输入用户名" maxlength="11" show-word-limit clearable />
              </el-form-item>
              <el-form-item prop="mail" label="邮箱">
                <el-input v-model="addForm.mail" placeholder="请输入邮箱" show-word-limit clearable />
              </el-form-item>
              <el-form-item prop="phone" label="手机号">
                <el-input v-model="addForm.phone" placeholder="请输入手机号" show-word-limit clearable />
              </el-form-item>
              <el-form-item prop="realName" label="姓名">
                <el-input v-model="addForm.realName" placeholder="请输入姓名" show-word-limit clearable />
              </el-form-item>
              <el-form-item prop="password" label="密码">
                <el-input v-model="addForm.password" type="password" clearable placeholder="请输入密码" show-password />
              </el-form-item>
              <el-button
                :loading="loading"
                @keyup.enter="login"
                type="primary"
                class="primary-btn"
                @click="addUser(loginFormRef2)"
              >
                创建账号
              </el-button>
              <div class="register-note">演示环境可能限制注册权限。</div>
            </el-form>
          </div>
        </div>
      </section>
    </main>
  </div>
  <el-dialog v-model="isWC" title="人机验证" width="40%" :before-close="handleClose">
    <div class="verification-flex">
      <span>扫码下方二维码，关注后回复：<strong><span style="color:blue;">link</span></strong>人机验证码</span>
      <img class="img" src="@/assets/png/公众号二维码.png" alt="">
      <el-form class="form" :model="verification" :rules="verificationRule" ref="verificationRef">
        <el-form-item prop="code" label="验证码">
          <el-input v-model="verification.code" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="isWC = false">取消</el-button>
        <el-button type="primary" @click="verificationLogin(verificationRef)">
          确认
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { setToken, setUsername, getUsername } from '@/core/auth.js'
import { ref, reactive, onMounted, onBeforeUnmount, watch, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const { proxy } = getCurrentInstance()
const API = proxy.$API
const loginFormRef1 = ref()
const loginFormRef2 = ref()
const router = useRouter()
const loginForm = reactive({
  username: 'heng123',
  password: '12345678',
})
const addForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  mail: ''
})

const addFormRule = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3|5|7|8|9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    },
    { min: 11, max: 11, message: '手机号必须是11位', trigger: 'blur' }
  ],
  username: [{ required: true, message: '请输入您的真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 15, message: '密码长度请在八位以上', trigger: 'blur' }
  ],
  mail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      pattern: /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/,
      message: '请输入正确的邮箱号',
      trigger: 'blur'
    }
  ],
  realNamee: [
    { required: true, message: '请输姓名', trigger: 'blur' },
  ]
})
const loginFormRule = reactive({
  username: [{ required: true, message: '请输入您的真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 15, message: '密码长度请在八位以上', trigger: 'blur' }
  ],
})
// 注册
const addUser = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      // 检测用户名是否已经存在
      const res1 = await API.user.hasUsername({ username: addForm.username })
      if (res1.data.success !== false) {
        // 注册
        const res2 = await API.user.addUser(addForm)
        // console.log(res2)
        if (res2.data.success === false) {
          ElMessage.warning(res2.data.message)
        } else {
          const res3 = await API.user.login({ username: addForm.username, password: addForm.password })
          const token = res3?.data?.data?.token
          // 将username和token保存到cookies中和localStorage中
          if (token) {
            setToken(token)
            setUsername(addForm.username)
            localStorage.setItem('token', token)
            localStorage.setItem('username', addForm.username)
          }
          ElMessage.success('注册登录成功！')
          router.push('/home')
        }
      } else {
        ElMessage.warning('用户名已存在！')
      }
    } else {
      return false
    }
  })

}
// 公众号验证码
const isWC = ref(false)
const verificationRef = ref()
const verification = reactive({
  code: ''
})
const verificationRule = reactive({
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})
const verificationLogin = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      const tempPassword = loginForm.password
      loginForm.password = verification.code
      const res1 = await API.user.login(loginForm)
      if (res1.data.code === '0') {
        const token = res1?.data?.data?.token
        // 将username和token保存到cookies中和localStorage中
        if (token) {
          setToken(token)
          setUsername(loginForm.username)
          localStorage.setItem('token', token)
          localStorage.setItem('username', loginForm.username)
        }
        ElMessage.success('登录成功！')
        router.push('/home')
      } else if (res1.data.message === '用户已登录') {
        // 如果已经登录了，判断一下浏览器保存的登录信息是不是再次登录的信息，如果是就正常登录
        const cookiesUsername = getUsername()
        if (cookiesUsername === loginForm.username) {
          ElMessage.success('登录成功！')
          router.push('/home')
        } else {
          ElMessage.warning('用户已在别处登录，请勿重复登录！')
        }
      } else {
        ElMessage.error('请输入正确的验证码!')
      }
      loginForm.password = tempPassword
    }
  })
}
// 登录
const login = (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      // 当域名为下面这两个时，弹出公众号弹框
      // let domain = window.location.host
      // if (domain === 'shortlink.magestack.cn' || domain === 'shortlink.nageoffer.com') {
      //   isWC.value = true
      //   return
      // }
      const res1 = await API.user.login(loginForm)
      if (res1.data.code === '0') {
        const token = res1?.data?.data?.token
        // 将username和token保存到cookies中和localStorage中
        if (token) {
          setToken(token)
          setUsername(loginForm.username)
          localStorage.setItem('token', token)
          localStorage.setItem('username', loginForm.username)
        }
        ElMessage.success('登录成功！')
        router.push('/home')
      } else if (res1.data.message === '用户已登录') {
        // 如果已经登录了，判断一下浏览器保存的登录信息是不是再次登录的信息，如果是就正常登录
        const cookiesUsername = getUsername()
        if (cookiesUsername === loginForm.username) {
          ElMessage.success('登录成功！')
          router.push('/home')
        } else {
          ElMessage.warning('用户已在别处登录，请勿重复登录！')
        }
      } else if (res1.data.message === '用户不存在') {
        ElMessage.error('请输入正确的账号密码!')
      }
    } else {
      return false
    }
  })


}

const loading = ref(false)
// 是否记住密码
const checked = ref(true)
const vantaRef = ref()
// 动态背景
let vantaEffect = null
let vantaCancelled = false
const shouldEnableVanta = () => {
  if (typeof window === 'undefined') {
    return false
  }
  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return false
  }
  return window.innerWidth > 768
}
const initVanta = async () => {
  if (!vantaRef.value || !shouldEnableVanta()) {
    return
  }
  const [{ default: NET }, THREE] = await Promise.all([
    import('vanta/src/vanta.net'),
    import('three')
  ])
  if (vantaCancelled || !vantaRef.value) {
    return
  }
  vantaEffect = NET({
    el: vantaRef.value,
    THREE,
    mouseControls: true,
    touchControls: true,
    gyroControls: false,
    minHeight: 200.0,
    minWidth: 200.0,
    scale: 1.0,
    scaleMobile: 1.0,
    color: 0x38f6ff,
    backgroundAlpha: 0.0,
    points: 14.0,
    maxDistance: 22.0,
    spacing: 18.0
  })
}
onMounted(() => {
  const startVanta = () => {
    initVanta().catch(() => {})
  }
  if (typeof window !== 'undefined' && 'requestIdleCallback' in window) {
    window.requestIdleCallback(startVanta, { timeout: 1200 })
  } else {
    setTimeout(startVanta, 300)
  }
})
onBeforeUnmount(() => {
  vantaCancelled = true
  if (vantaEffect) {
    vantaEffect.destroy()
    vantaEffect = null
  }
})
// 展示登录还是展示注册
const isLogin = ref(true)
const changeLogin = (targetLogin) => {
  let domain = window.location.host
  if (typeof targetLogin === 'boolean') {
    isLogin.value = targetLogin
    return
  }
  isLogin.value = !isLogin.value
}
</script>

<style lang="less" scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&family=Space+Grotesk:wght@400;500;600;700&display=swap');

.login-page {
  --ink: #e9f6ff;
  --ink-soft: #8aa3bd;
  --accent: #38f6ff;
  --accent-strong: #20c7ff;
  --sea: #1de0c6;
  --sea-soft: #7ff7e1;
  --card: rgba(7, 14, 28, 0.78);
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  color: var(--ink);
  background: radial-gradient(circle at 15% 20%, #1b2a4f 0%, #0b1226 45%, #070a14 100%);
  padding: 32px 48px 48px;
  box-sizing: border-box;
}

.bg-aurora {
  position: absolute;
  inset: -20% -10% auto -10%;
  height: 70%;
  background: radial-gradient(circle at 20% 30%, rgba(56, 246, 255, 0.35), transparent 55%),
    radial-gradient(circle at 80% 20%, rgba(49, 140, 255, 0.35), transparent 55%),
    radial-gradient(circle at 40% 80%, rgba(29, 224, 198, 0.35), transparent 60%);
  filter: blur(24px);
  z-index: 1;
  pointer-events: none;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(56, 246, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(56, 246, 255, 0.08) 1px, transparent 1px);
  background-size: 48px 48px;
  z-index: 2;
  opacity: 0.4;
  pointer-events: none;
}

.bg-beam {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, transparent 20%, rgba(56, 246, 255, 0.22) 50%, transparent 80%);
  mix-blend-mode: screen;
  animation: sweep 12s linear infinite;
  z-index: 3;
  pointer-events: none;
}

.vanta {
  position: absolute;
  inset: 0;
  z-index: 0;
  opacity: 0.85;
}

.login-header {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0b1020, #16325a);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: 0 0 18px rgba(56, 246, 255, 0.4);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
}

.brand-sub {
  font-size: 13px;
  color: var(--ink-soft);
}

.status-pill {
  padding: 6px 14px;
  background: rgba(56, 246, 255, 0.14);
  border-radius: 999px;
  font-size: 12px;
  color: var(--ink);
}

.login-shell {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 0.9fr);
  gap: 36px;
  height: calc(100vh - 140px);
  align-items: center;
}

.hero-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 560px;
  animation: rise 0.8s ease;
}

.hero-badge {
  align-self: flex-start;
  background: rgba(56, 246, 255, 0.18);
  color: #e9f6ff;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.hero-panel h1 {
  font-size: 42px;
  line-height: 1.1;
  margin: 0;
}

.hero-panel p {
  font-size: 16px;
  line-height: 1.7;
  color: var(--ink-soft);
  margin: 0;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  background: rgba(7, 14, 28, 0.65);
  border: 1px solid rgba(56, 246, 255, 0.2);
  padding: 16px;
  border-radius: 18px;
  box-shadow: 0 16px 30px rgba(5, 10, 22, 0.6), 0 0 16px rgba(56, 246, 255, 0.15);
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--ink);
}

.metric-label {
  font-size: 12px;
  color: var(--ink-soft);
}

.hero-footnote {
  font-size: 13px;
  color: var(--ink-soft);
  background: rgba(10, 18, 34, 0.7);
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid rgba(56, 246, 255, 0.18);
}

.auth-panel {
  display: flex;
  justify-content: flex-end;
  animation: fadeIn 0.9s ease;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: var(--card);
  border-radius: 28px;
  padding: 28px;
  box-shadow: 0 30px 60px rgba(3, 6, 14, 0.6), inset 0 0 0 1px rgba(56, 246, 255, 0.18);
  border: 1px solid rgba(56, 246, 255, 0.18);
  backdrop-filter: blur(8px);
}

.auth-switch {
  display: inline-flex;
  padding: 4px;
  background: rgba(11, 20, 38, 0.7);
  border-radius: 999px;
  gap: 4px;
}

.auth-switch button {
  border: none;
  background: transparent;
  color: var(--ink-soft);
  padding: 8px 18px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.auth-switch button.active {
  background: linear-gradient(135deg, rgba(56, 246, 255, 0.85), rgba(32, 199, 255, 0.85));
  color: #0b1226;
  box-shadow: 0 8px 18px rgba(56, 246, 255, 0.35);
}

.auth-title {
  margin-top: 18px;
  margin-bottom: 18px;
}

.auth-title h2 {
  font-size: 26px;
  margin: 0 0 6px 0;
}

.auth-title p {
  margin: 0;
  color: var(--ink-soft);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-soft);
}

.helper-text {
  font-size: 12px;
  color: var(--ink-soft);
}

.primary-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent), var(--accent-strong));
  border: none;
  box-shadow: 0 14px 24px rgba(32, 199, 255, 0.35);
}

.register-note {
  margin-top: 12px;
  font-size: 12px;
  color: var(--ink-soft);
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--ink);
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 6px 12px;
  box-shadow: none;
  border: 1px solid rgba(56, 246, 255, 0.2);
  background: rgba(6, 14, 28, 0.8);
}

:deep(.el-input__inner) {
  font-family: inherit;
  color: var(--ink);
}

:deep(.el-input__inner::placeholder) {
  color: rgba(154, 179, 199, 0.8);
}

:deep(.el-checkbox__label) {
  color: var(--ink-soft);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--accent), var(--accent-strong));
  border: none;
}

.verification-flex {
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  .img {
    margin-top: 10px;
    align-self: center;
  }

  .form {
    transform: translateY(15px);
    width: 90%;
  }
}

@keyframes fadeIn {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes rise {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes sweep {
  0% {
    transform: translateX(-40%) translateY(0);
    opacity: 0.2;
  }

  50% {
    opacity: 0.6;
  }

  100% {
    transform: translateX(40%) translateY(-10%);
    opacity: 0.2;
  }
}

@media (max-width: 980px) {
  .login-page {
    padding: 24px;
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .login-shell {
    grid-template-columns: 1fr;
    height: auto;
  }

  .hero-panel {
    max-width: 100%;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .auth-panel {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .login-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .hero-panel h1 {
    font-size: 32px;
  }
}
</style>
