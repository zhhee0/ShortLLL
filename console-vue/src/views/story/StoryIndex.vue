<template>
  <div class="story-page">
    <div class="story-top">
      <div class="hero-left glass-panel hero-overlay">
        <p class="hero-eyebrow">STORY LAB</p>
        <h1 class="hero-title">故事集（娱乐）</h1>
        <p class="hero-subtitle">把你的灵感写进星轨，留给未来的自己。</p>
        <div class="hero-stats">
          <div class="stat-card">
            <div class="stat-value">{{ storyTotal }}</div>
            <div class="stat-label">已发布</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ draftWordCount }}</div>
            <div class="stat-label">当前字数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">LIVE</div>
            <div class="stat-label">故事模式</div>
          </div>
        </div>
      </div>
    </div>

    <div class="story-hero">
      <div class="hero-right">
        <div class="glass-panel carousel-panel">
          <div class="panel-title">故事影像</div>
          <div v-if="!carouselItems.length" class="carousel-placeholder">
            <span>暂无影像</span>
          </div>
          <el-carousel
            v-else
            height="calc(100vh - 50px)"
            indicator-position="outside"
            :interval="4200"
            trigger="click"
            arrow="always"
            @change="handleCarouselChange"
          >
            <el-carousel-item v-for="(item, index) in carouselItems" :key="item.src">
              <img
                v-if="item.type === 'image'"
                class="carousel-img"
                :src="item.src"
                alt="story"
                :loading="index < eagerLoadCount ? 'eager' : 'lazy'"
                :fetchpriority="index < eagerLoadCount ? 'high' : 'low'"
                decoding="async"
              />
              <video
                v-else
                class="carousel-video"
                :src="item.src"
                preload="metadata"
                autoplay
                muted
                loop
                playsinline
                controls
              ></video>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </div>

    <div class="story-grid" :class="{ 'public-view': isPublicView }">
      <div v-if="canPublish" class="glass-panel editor-panel">
        <div class="panel-header">
          <span>写下你的故事</span>
          <span class="panel-tip">发布后公开可见</span>
        </div>
        <div class="editor-body">
          <el-input v-model="draft.title" maxlength="32" placeholder="给故事起个名字" />
          <el-input
            v-model="draft.content"
            type="textarea"
            :autosize="{ minRows: 6, maxRows: 14 }"
            maxlength="1200"
            placeholder="把你的灵感写下来..."
            show-word-limit
          />
        </div>
        <div class="editor-actions">
          <el-button class="neon-btn" type="primary" @click="publishStory">发布到故事墙</el-button>
          <el-button class="ghost-btn" @click="clearDraft">清空草稿</el-button>
        </div>
      </div>

      <div class="glass-panel wall-panel">
        <div class="panel-header">
          <span>故事墙</span>
          <div class="panel-actions">
            <span class="panel-tip">{{ storyCount }} / {{ storyTotal }}</span>
            <el-button
              v-if="canPublish"
              class="ghost-btn"
              plain
              size="small"
              @click="clearMyStories"
              >清空我的故事</el-button
            >
          </div>
        </div>
        <div v-if="stories.length" class="story-list">
          <div v-for="story in stories" :key="story.id" class="story-card">
            <el-button
              v-if="canPublish && isAdmin"
              class="story-delete-btn"
              type="danger"
              text
              @click="deleteStory(story)"
            >
              删除
            </el-button>
            <div class="story-title">{{ story.title }}</div>
            <div class="story-meta">
              <div class="story-meta-left">
                <span v-if="story.username">{{ story.username }} - </span>{{ formatTime(story.createTime) }}
              </div>
              <el-button
                class="story-like-btn"
                link
                :type="story.liked ? 'warning' : 'primary'"
                :class="{ 'is-liked': story.liked }"
                :loading="isStoryLiking(story.id)"
                @click="likeStory(story)"
              >
                <el-icon class="story-like-icon">
                  <Pointer />
                </el-icon>
                <span class="story-like-count">{{ story.likeCount || 0 }}</span>
              </el-button>
            </div>
            <div class="story-content">{{ story.content }}</div>
          </div>
        </div>
        <div v-else class="story-empty">
          <el-empty description="还没有故事，先写下第一篇吧" />
        </div>
        <div v-if="storyTotal > pageSize" class="story-pagination">
          <el-pagination
            background
            :layout="paginationLayout"
            :total="storyTotal"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="pageSizes"
            :pager-count="isMobile ? 5 : 7"
            :small="isMobile"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>

    <div class="story-glow glow-1"></div>
    <div class="story-glow glow-2"></div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, getCurrentInstance } from 'vue'
import { Pointer } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { getToken, getUsername } from '@/core/auth.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance()
const API = proxy.$API
const route = useRoute()

const DRAFT_KEY = 'shortlink.story.draft.v1'
const STORY_LIMIT = 200
const pageSizes = [10, 20, 50, 100, 200]
const pageSize = ref(20)
const currentPage = ref(1)
const isMobile = ref(typeof window !== 'undefined' ? window.innerWidth <= 768 : false)
const updateIsMobile = () => {
  isMobile.value = window.innerWidth <= 768
}
const paginationLayout = computed(() =>
  isMobile.value ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'
)
const eagerLoadCount = computed(() => (isMobile.value ? 2 : 4))

const draft = reactive({
  title: '',
  content: ''
})

const stories = ref([])
const likePending = reactive(new Set())
const isStoryLiking = (id) => likePending.has(id)
const storyTotal = ref(0)
const isPublicView = computed(() => route.meta?.public === true)
const canPublish = computed(() => Boolean(getToken()))
const isAdmin = computed(() => getUsername() === 'admin')
const carouselModules = import.meta.glob('@/assets/story/*.{jpg,jpeg,png,webp,mp4}', {
  eager: true,
  import: 'default'
})
const buildCarouselItems = () => {
  const collator = new Intl.Collator('zh-CN', { numeric: true, sensitivity: 'base' })
  return Object.entries(carouselModules)
    .map(([path, src]) => {
      const lowerPath = path.toLowerCase()
      return {
        type: lowerPath.endsWith('.mp4') ? 'video' : 'image',
        src,
        name: path.split('/').pop() || ''
      }
    })
    .sort((a, b) => collator.compare(a.name, b.name))
    .map(({ name, ...rest }) => rest)
}
const fullCarouselItems = buildCarouselItems()
const carouselItems = ref([])
const preloadedImages = new Set()

const preloadImage = (src) => {
  if (!src || preloadedImages.has(src) || typeof window === 'undefined') {
    return
  }
  const img = new Image()
  preloadedImages.add(src)
  const cleanup = () => {
    preloadedImages.delete(src)
  }
  img.onload = cleanup
  img.onerror = cleanup
  img.src = src
}

const preloadAroundIndex = (index) => {
  if (!fullCarouselItems.length) {
    return
  }
  const windowSize = isMobile.value ? 2 : 4
  for (let offset = 0; offset <= windowSize; offset += 1) {
    const item = fullCarouselItems[(index + offset) % fullCarouselItems.length]
    if (item?.type === 'image') {
      preloadImage(item.src)
    }
  }
}

const preloadAllImages = () => {
  if (!fullCarouselItems.length || typeof window === 'undefined') {
    return
  }
  const images = fullCarouselItems.filter((item) => item.type === 'image')
  let index = 0
  const step = () => {
    const item = images[index]
    if (!item) {
      return
    }
    preloadImage(item.src)
    index += 1
    if (index < images.length) {
      if ('requestIdleCallback' in window) {
        window.requestIdleCallback(step, { timeout: 1500 })
      } else {
        setTimeout(step, 120)
      }
    }
  }
  step()
}


const hydrateCarousel = () => {
  if (carouselItems.value.length >= fullCarouselItems.length) {
    return
  }
  carouselItems.value = fullCarouselItems
}

const scheduleHydrateCarousel = () => {
  const hydrate = () => {
    if (carouselItems.value.length < fullCarouselItems.length) {
      carouselItems.value = fullCarouselItems
    }
  }
  if (typeof window !== 'undefined' && 'requestIdleCallback' in window) {
    window.requestIdleCallback(hydrate, { timeout: 1500 })
  } else {
    setTimeout(hydrate, 300)
  }
}

const initCarousel = () => {
  const initialCount = isMobile.value ? 1 : 3
  carouselItems.value = fullCarouselItems.slice(0, initialCount)
  preloadAroundIndex(0)
  if (initialCount < fullCarouselItems.length) {
    scheduleHydrateCarousel()
  }
  preloadAllImages()
}

const storyCount = computed(() => stories.value.length)
let storyRequestSeq = 0
const draftWordCount = computed(
  () => (draft.title?.length || 0) + (draft.content?.length || 0)
)

const saveDraft = () => {
  localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
}

const queryStories = async () => {
  const size = Math.min(pageSize.value, STORY_LIMIT)
  if (pageSize.value != size) {
    pageSize.value = size
  }
  const requestId = ++storyRequestSeq
  const isFirstPage = currentPage.value === 1
  const previewSize = isFirstPage ? Math.min(6, size) : size
  try {
    const res = await API.story.pageStory({ current: currentPage.value, size: previewSize })
    if (requestId !== storyRequestSeq) {
      return
    }
    const pageData = res?.data?.data
    stories.value = pageData?.records || []
    storyTotal.value = +pageData?.total || stories.value.length
    if (previewSize === size) {
      return
    }
    const resFull = await API.story.pageStory({ current: currentPage.value, size })
    if (requestId !== storyRequestSeq) {
      return
    }
    const fullData = resFull?.data?.data
    stories.value = fullData?.records || stories.value
    storyTotal.value = +fullData?.total || storyTotal.value
  } catch (error) {
    if (requestId === storyRequestSeq) {
      ElMessage.error('????????????')
    }
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  queryStories()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  queryStories()
}

const publishStory = async () => {
  if (!canPublish.value) {
    ElMessage.warning('请先登录再发布')
    return
  }
  if (!draft.title.trim() || !draft.content.trim()) {
    ElMessage.warning('标题和内容都要写哦')
    return
  }
  try {
    await API.story.createStory({
      title: draft.title.trim(),
      content: draft.content.trim()
    })
    draft.title = ''
    draft.content = ''
    saveDraft()
    ElMessage.success('故事已发布')
    currentPage.value = 1
    queryStories()
  } catch (error) {
    ElMessage.error('发布失败，请稍后再试')
  }
}

const clearDraft = () => {
  draft.title = ''
  draft.content = ''
  saveDraft()
}

const clearMyStories = async () => {
  try {
    await ElMessageBox.confirm('确定要清空你的故事吗？此操作不可恢复。', '提示', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    return
  }
  try {
    await API.story.clearMyStory()
    ElMessage.success('已清空你的故事')
    currentPage.value = 1
    queryStories()
  } catch (error) {
    ElMessage.error('清空失败，请稍后再试')
  }
}

const deleteStory = async (story) => {
  if (!story?.id) {
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除这条故事吗？此操作不可恢复。', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    return
  }
  try {
    const res = await API.story.deleteStory({ id: String(story.id) })
    if (res?.data?.code !== '0') {
      ElMessage.error(res?.data?.message || '删除失败，请稍后再试')
      return
    }
    ElMessage.success('已删除故事')
    queryStories()
  } catch (error) {
    ElMessage.error('删除失败，请稍后再试')
  }
}


const likeStory = async (story) => {
  if (!story?.id) {
    return
  }
  if (!canPublish.value) {
    ElMessage.warning('???????')
    return
  }
  if (likePending.has(story.id)) {
    return
  }
  likePending.add(story.id)
  try {
    const res = await API.story.likeStory({ id: String(story.id) })
    if (res?.data?.code !== '0') {
      ElMessage.error(res?.data?.message || '??????????')
      return
    }
    await queryStories()
  } catch (error) {
    ElMessage.error('??????????')
  } finally {
    likePending.delete(story.id)
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) {
    return ''
  }
  if (typeof timestamp === 'string' && timestamp.includes('-')) {
    return timestamp
  }
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleCarouselChange = (currentIndex) => {
  preloadAroundIndex(currentIndex)
}

onMounted(() => {
  updateIsMobile()
  initCarousel()
  window.addEventListener('resize', updateIsMobile)
  const draftCache = localStorage.getItem(DRAFT_KEY)
  if (draftCache) {
    try {
      const parsedDraft = JSON.parse(draftCache)
      draft.title = parsedDraft?.title || ''
      draft.content = parsedDraft?.content || ''
    } catch (error) {
      draft.title = ''
      draft.content = ''
    }
  }
  queryStories()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateIsMobile)
})

watch(
  isMobile,
  (value) => {
    if (!value) {
      hydrateCarousel()
    }
  }
)

watch(
  () => [draft.title, draft.content],
  () => {
    saveDraft()
  }
)
</script>


<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;600&family=Space+Grotesk:wght@400;600&display=swap') screen and (min-width: 961px);

.story-page {
  position: relative;
  min-height: calc(100vh - 50px);
  padding: 0 0 64px;
  color: #e6f6ff;
  background:
    radial-gradient(900px 520px at 12% 12%, rgba(66, 255, 246, 0.18), transparent 60%),
    radial-gradient(800px 520px at 85% 0%, rgba(120, 170, 255, 0.18), transparent 58%),
    linear-gradient(180deg, #050b16 0%, #081424 48%, #0b1b2e 100%);
  overflow: hidden;
  font-family: 'Space Grotesk', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.story-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 48px 48px;
  opacity: 0.22;
  pointer-events: none;
}

.story-top {
  position: relative;
  z-index: 2;
  padding: 24px clamp(16px, 3vw, 32px) 0;
}

.story-hero {
  position: relative;
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 28px;
  align-items: center;
  z-index: 1;
}

.hero-left {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
  text-align: left;
  max-width: 420px;
  z-index: 2;
  margin: 0;
}

.hero-right {
  display: flex;
  justify-content: center;
  width: 100%;
  z-index: 1;
}

.carousel-panel {
  width: 100%;
  height: 100%;
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 0;
  backdrop-filter: none;
}

.carousel-panel :deep(.el-carousel) {
  max-height: none;
  min-height: 360px;
}

.carousel-panel :deep(.el-carousel__container) {
  background: rgba(5, 11, 22, 0.6);
}

.carousel-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  height: calc(100vh - 50px);
  color: rgba(231, 246, 255, 0.7);
  background: rgba(5, 11, 22, 0.6);
  letter-spacing: 0.1em;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 0;
  background: rgba(5, 11, 22, 0.5);
}

.carousel-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 0;
  background: rgba(5, 11, 22, 0.5);
}

.carousel-panel :deep(.el-carousel) {
  border-radius: 0;
  overflow: hidden;
}

.carousel-panel .panel-title {
  display: none;
}

.hero-overlay {
  background: rgba(5, 11, 22, 0.55);
  border: 1px solid rgba(120, 200, 255, 0.24);
  box-shadow: 0 24px 60px rgba(3, 9, 18, 0.6);
}

.hero-eyebrow {
  font-family: 'Orbitron', sans-serif;
  font-size: 12px;
  letter-spacing: 0.4em;
  color: rgba(148, 232, 255, 0.7);
  text-transform: uppercase;
}

.hero-title {
  font-family: 'Orbitron', sans-serif;
  font-size: clamp(32px, 4vw, 44px);
  margin: 0;
  background: linear-gradient(120deg, #6ffbff, #9a7bff, #5fd1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 16px;
  line-height: 1.7;
  color: rgba(231, 246, 255, 0.75);
  max-width: 520px;
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-start;
}

.stat-card {
  padding: 12px 16px;
  border-radius: 14px;
  background: rgba(10, 24, 42, 0.45);
  border: 1px solid rgba(120, 200, 255, 0.2);
  min-width: 110px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #7ef4ff;
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(231, 246, 255, 0.6);
}

.glass-panel {
  position: relative;
  border-radius: 20px;
  padding: 24px;
  background: rgba(10, 22, 38, 0.55);
  border: 1px solid rgba(120, 200, 255, 0.16);
  backdrop-filter: blur(18px);
  box-shadow: 0 22px 60px rgba(3, 9, 18, 0.55);
}

.hero-panel {
  min-height: 200px;
}

.panel-title {
  font-size: 14px;
  color: rgba(231, 246, 255, 0.6);
  margin-bottom: 10px;
}

.panel-highlight {
  font-size: 20px;
  font-weight: 600;
}

.panel-divider {
  height: 1px;
  margin: 14px 0;
  background: linear-gradient(90deg, rgba(127, 248, 255, 0.6), transparent);
}

.panel-text {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(231, 246, 255, 0.7);
}

.story-grid {
  position: relative;
  z-index: 1;
  margin-top: 32px;
  padding: 0 clamp(20px, 4vw, 64px);
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.story-grid.public-view {
  grid-template-columns: 1fr;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-tip {
  font-size: 12px;
  color: rgba(231, 246, 255, 0.6);
}

.editor-body {
  display: grid;
  gap: 16px;
}

.editor-actions {
  display: flex;
  gap: 12px;
  margin-top: 18px;
}

.neon-btn {
  background: linear-gradient(120deg, #5ffcff, #8d7bff);
  border: none;
  color: #061321;
  font-weight: 600;
  box-shadow: 0 12px 30px rgba(93, 200, 255, 0.35);
}

.ghost-btn {
  background: transparent;
  color: #c8e6ff;
  border: 1px solid rgba(140, 220, 255, 0.4);
}

.story-list {
  display: grid;
  gap: 16px;
}

.story-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.story-card {
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(7, 16, 29, 0.65);
  border: 1px solid rgba(130, 220, 255, 0.16);
  position: relative;
  max-width: 100%;
  overflow-x: auto;
}

.story-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.story-delete-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  color: rgba(255, 140, 140, 0.9);
}

.story-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  color: rgba(231, 246, 255, 0.6);
  margin-bottom: 10px;
}

.story-meta-left {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.story-like-btn {
  gap: 6px;
  padding: 0;
  position: relative;
  overflow: hidden;
  -webkit-tap-highlight-color: transparent;
}

.story-like-btn:active,
.story-like-btn:hover {
  background: transparent;
  box-shadow: none;
  outline: none;
}

.story-like-icon {
  font-size: 16px;
}

.story-like-btn.is-liked .story-like-icon {
  animation: story-like-pop 260ms ease;
  filter: drop-shadow(0 0 6px currentColor);
  transform-origin: center;
}

.story-like-btn::after {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 999px;
  background: radial-gradient(circle, currentColor 0%, rgba(0, 0, 0, 0) 70%);
  opacity: 0;
  transform: scale(0.2);
  pointer-events: none;
}

.story-like-btn:active::after {
  animation: story-like-ripple 420ms ease-out;
}

@keyframes story-like-pop {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.18);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes story-like-ripple {
  0% {
    opacity: 0.35;
    transform: scale(0.2);
  }
  100% {
    opacity: 0;
    transform: scale(1.6);
  }
}

.story-like-count {
  font-size: 12px;
  font-weight: 600;
  color: currentColor;
}

.story-content {
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  color: rgba(231, 246, 255, 0.75);
  overflow-wrap: anywhere;
  word-break: break-word;
}

.story-empty {
  padding: 24px 0 10px;
}

.story-glow {
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.45;
  pointer-events: none;
}

.glow-1 {
  background: #6ffbff;
  top: -60px;
  right: 12%;
}

.glow-2 {
  background: #7b7cff;
  bottom: -80px;
  left: 6%;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: rgba(8, 18, 32, 0.7);
  border: 1px solid rgba(130, 220, 255, 0.2);
  box-shadow: none;
  color: #e6f6ff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  border-color: rgba(111, 251, 255, 0.7);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: #e6f6ff;
}

:deep(.el-input__count),
:deep(.el-textarea__count) {
  color: rgba(231, 246, 255, 0.6);
}

:deep(.el-carousel__indicator button) {
  background-color: rgba(231, 246, 255, 0.35);
}

:deep(.el-carousel__indicator.is-active button) {
  background-color: rgba(111, 251, 255, 0.9);
}

@media (max-width: 960px) {
  .story-page {
    padding-bottom: 110px;
    overflow-x: hidden;
    width: 100%;
    max-width: 100vw;
  }

  .story-page::before {
    opacity: 0.14;
  }

  .story-top {
    display: flex;
    justify-content: center;
    padding: 24px 16px 0;
  }

  .story-hero {
    min-height: calc(100vh - 50px);
  }

  .hero-left {
    align-items: center;
    text-align: center;
    max-width: 92%;
    margin: 0 auto;
  }

  .carousel-panel {
    width: 100%;
  }

  .carousel-panel :deep(.el-carousel) {
    height: calc(100vh - 50px);
  }

  .story-grid {
    grid-template-columns: 1fr;
  }

  .story-grid {
    margin-top: 20px;
    padding: 0 14px 24px;
    gap: 16px;
    justify-items: start;
    width: 100%;
    max-width: 100vw;
    margin-left: 0;
    margin-right: 0;
    box-sizing: border-box;
  }

  .story-grid .glass-panel {
    padding: 16px;
    border-radius: 16px;
    width: 100%;
    max-width: 100%;
    margin-left: 0;
    margin-right: 0;
    box-sizing: border-box;
    align-self: start;
  }

  .glass-panel {
    backdrop-filter: none;
    box-shadow: 0 12px 30px rgba(3, 9, 18, 0.4);
  }

  .story-grid .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    text-align: left;
  }

  .story-grid .panel-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .story-grid .panel-actions .ghost-btn {
    width: 100%;
  }

  .story-grid .panel-tip {
    line-height: 1.4;
  }

  .story-grid .editor-body,
  .story-grid .story-list {
    align-items: flex-start;
    text-align: left;
  }

  .editor-actions {
    flex-direction: column;
  }

  .story-pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    gap: 6px;
  }

  .story-pagination {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    margin-top: 0;
    padding: 8px 12px calc(8px + env(safe-area-inset-bottom, 0px));
    background: rgba(5, 11, 22, 0.92);
    border-top: 1px solid rgba(120, 200, 255, 0.18);
    backdrop-filter: blur(12px);
    z-index: 10;
  }

  .hero-stats {
    justify-content: center;
  }
}

@media (hover: none) and (pointer: coarse) {
  .story-like-btn:focus,
  .story-like-btn:focus-visible {
    outline: none;
    box-shadow: none;
  }
}
</style>
