<template>
  <div class="stats-page">
    <div class="stats-header">
      <el-button class="back-btn" text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-main">
        <img v-if="!isGroup" :src="getImgUrl(favicon)" width="28" height="28" alt="" />
        <div class="header-text">
          <div class="title">{{ statsTitle }}</div>
          <div v-if="!isGroup" class="origin">{{ originUrl }}</div>
        </div>
      </div>
      <div v-if="isGroup" class="group-count">共{{ nums }}条短链接</div>
    </div>

    <div class="stats-toolbar">
      <el-date-picker
        v-model="dateValue"
        :clearable="true"
        type="daterange"
        range-separator="To"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD"
        :shortcuts="shortcuts"
      />
    </div>

    <el-tabs v-model="showPane">
      <el-tab-pane name="访问数据" label="访问数据">
        <div class="content-box scroll-box">
          <div ref="lineSectionRef">
            <TitleContent class="chart-item" title="访问曲线" @onMounted="requestLineRender">
              <template #titleButton>
                <div>
                  <el-button @click="isLine = !isLine">切换为曲线</el-button>
                </div>
              </template>
              <template #content>
                <div v-if="sectionVisible.line" class="list-chart">
                  <div v-show="isLine" class="top10" style="padding-top: 20px">
                    <div class="key-value" style="margin-top: 10px">
                      <span>访问次数</span>
                      <span>{{ totalPv }}</span>
                    </div>
                    <div class="key-value" style="margin-top: 10px">
                      <span>访问人数</span>
                      <span>{{ totalUv }}</span>
                    </div>
                    <div class="key-value" style="margin-top: 10px">
                      <span>访问IP数</span>
                      <span>{{ totalUip }}</span>
                    </div>
                  </div>
                  <div v-show="isLine" class="lineChart"></div>
                  <div v-show="!isLine" style="padding: 20px">
                    <el-table :data="visitsData" border style="width: 100%; height: 210px; overflow: scroll"
                      :header-cell-style="{ background: '#eef1f6', color: '#606266' }">
                      <el-table-column prop="date" label="时间" width="160" />
                      <el-table-column prop="pv" label="访问次数" width="160" />
                      <el-table-column prop="uv" label="访问人数" width="160" />
                      <el-table-column prop="uip" label="访问IP数" width="160" />
                    </el-table>
                  </div>
                </div>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="mapSectionRef">
            <TitleContent class="chart-item" title="访问地区" @onMounted="requestMapRender">
              <template #content>
                <div v-if="sectionVisible.map" class="list-chart">
                  <div v-show="isChina" class="top10">
                    <span style="font-size: 14px">{{ chinaTopTitle }}</span>
                    <div class="top-item" v-for="(item, index) in chinaMapData" :key="item.name">
                      <div v-if="index <= 9" class="key-value">
                        <span>{{ index + 1 + '. ' + item.label }}</span>
                        <span>{{ (item.ratio * 100).toFixed(2) }}%</span>
                        <span>{{ item.value }}次</span>
                      </div>
                    </div>
                  </div>
                  <div v-show="!isChina" class="top10">
                    <span>TOP 10 国家</span>
                    <template v-for="(item, index) in worldMapData" :key="item.name">
                      <div v-if="index <= 9" class="key-value">
                        <span>{{ item.name }}</span>
                        <span>{{ item.value }}次</span>
                      </div>
                    </template>
                  </div>
                  <div v-show="isChina" class="chinaMap"></div>
                  <div v-show="!isChina" class="worldMap"></div>
                </div>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="hourSectionRef">
            <TitleContent class="chart-item" title="24小时分布">
              <template #content>
                <BarChart v-if="sectionVisible.hour" style="height: 100%; width: 100%" :chartData="{
                  xAxis: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23],
                  value: statsInfo?.hourStats || new Array(24).fill(0)
                }"></BarChart>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="topIpSectionRef">
            <TitleContent class="chart-item" title="高频访问IP" style="width: 390px">
              <template #content>
                <KeyValue v-if="sectionVisible.topIp" :dataLists="statsInfo?.topIpStats" style="height: 100%; width: 100%"></KeyValue>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="weekSectionRef">
            <TitleContent class="chart-item" title="一周分布" style="width: 390px">
              <template #content>
                <BarChart v-if="sectionVisible.week" style="height: 100%; width: 100%" :chartData="{
                  xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
                  value: statsInfo?.weekdayStats || new Array(7).fill(0)
                }"></BarChart>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="osSectionRef">
            <TitleContent class="chart-item" title="操作系统" style="width: 390px">
              <template #content>
                <ProgressLine v-if="sectionVisible.os" style="height: 100%; width: 100%" :dataLists="statsInfo?.osStats"></ProgressLine>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="browserSectionRef">
            <TitleContent class="chart-item" title="访问浏览器" style="width: 390px">
              <template #content>
                <ProgressLine v-if="sectionVisible.browser" style="height: 100%; width: 100%" :dataLists="statsInfo?.browserStats"></ProgressLine>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="userTypeSectionRef">
            <TitleContent v-if="!isGroup" class="chart-item" title="访客类型" style="width: 390px">
              <template #content>
                <ProgressPie v-if="sectionVisible.userType" style="height: 100%; width: 100%" :labels="['新访客', '旧访客']" :data="userTypeList"></ProgressPie>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="networkSectionRef">
            <TitleContent class="chart-item" title="访问网络" style="width: 390px">
              <template #content>
                <ProgressPie v-if="sectionVisible.network" style="height: 100%; width: 100%" :labels="['WIFI', '移动数据']" :data="netWorkList"></ProgressPie>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>

          <div ref="deviceSectionRef">
            <TitleContent class="chart-item" title="访问设备" style="width: 390px">
              <template #content>
                <ProgressPie v-if="sectionVisible.device" style="height: 100%; width: 100%" :labels="['电脑', '移动设备']" :data="deviceList"></ProgressPie>
                <div v-else class="chart-placeholder">加载中...</div>
              </template>
            </TitleContent>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="历史记录" label="历史记录">
        <el-table :data="tableInfo?.data?.data?.records" style="width: 100%; height: calc(100vh - 300px)">
          <el-table-column prop="createTime" label="访问时间" width="160" />
          <el-table-column prop="ip" label="访问IP" width="140" />
          <el-table-column prop="locale" label="访客地区"></el-table-column>
          <el-table-column prop="device" label="设备信息">
            <template #default="scope">
              <div class="flex-box">
                <img :src="getUrl1(scope?.row?.browser)" width="20" alt="" />
                <img :src="getUrl2(scope?.row?.os)" width="20" alt="" />
                <img :src="getUrl3(scope?.row?.device)" width="20" alt="" />
                <img :src="getUrl4(scope?.row?.network)" width="20" alt="" />
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!isGroup" prop="uvType" label="访客类型" />
        </el-table>
        <div class="pagination-block">
          <el-pagination v-model:current-page="statsFormData.current" v-model:page-size="statsFormData.size"
            :page-sizes="[10, 15, 20, 30]" layout="total, sizes, prev, pager, next, jumper" :total="totalNums"
            @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch, reactive, nextTick, onMounted, computed, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import TitleContent from './components/chartsInfo/TitleContent.vue'
let echartsRef = null
let echartsLoading = null
const mapModuleLoaded = {
  china: false,
  world: false
}
import BarChart from './components/chartsInfo/BarChart.vue'
import KeyValue from './components/chartsInfo/KeyValue.vue'
import ProgressLine from './components/chartsInfo/ProgressLine.vue'
import ProgressPie from './components/chartsInfo/ProgressPie.vue'
import edge from '@/assets/png/edge.png'
import Andriod from '@/assets/png/Andriod.png'
import Chorme from '@/assets/png/Chorme.png'
import firefox from '@/assets/png/firefox.png'
import iOS from '@/assets/png/iOS.png'
import macOS from '@/assets/png/macOS.png'
import other from '@/assets/png/other.png'
import Safair from '@/assets/png/Safair.png'
import WeChat from '@/assets/png/WeChat.png'
import Windows from '@/assets/png/Windows.png'
import linux from '@/assets/png/linux.png'
import wifi from '@/assets/png/wifi.png'
import PC from '@/assets/png/电脑.png'
import Mobile from '@/assets/png/移动设备.png'
import MobileDevices from '@/assets/png/移动设备.png'
import defaultImg from '@/assets/png/短链默认图标.png'
import opera from '@/assets/png/opera.png'
import IE from '@/assets/png/IE.png'
import { getTodayFormatDate, getLastWeekFormatDate } from '@/utils/plugins.js'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const API = proxy.$API

const sectionVisible = reactive({
  line: false,
  map: false,
  hour: false,
  topIp: false,
  week: false,
  os: false,
  browser: false,
  userType: false,
  network: false,
  device: false
})

const lineSectionRef = ref(null)
const mapSectionRef = ref(null)
const hourSectionRef = ref(null)
const topIpSectionRef = ref(null)
const weekSectionRef = ref(null)
const osSectionRef = ref(null)
const browserSectionRef = ref(null)
const userTypeSectionRef = ref(null)
const networkSectionRef = ref(null)
const deviceSectionRef = ref(null)

const lineReady = ref(false)
const lineNeedsRender = ref(false)
const mapReady = ref(false)
const mapNeedsRender = ref(false)

const isGroup = computed(() => {
  const value = route.query.group
  return value === true || value === 'true' || value === '1'
})
const statsTitle = computed(() => route.query.describe || '统计详情')
const originUrl = computed(() => route.query.originUrl || '')
const favicon = computed(() => route.query.favicon || '')
const nums = computed(() => Number(route.query.nums) || 0)
const fullShortUrl = computed(() => route.query.fullShortUrl || '')
const gid = computed(() => route.query.gid || '')
const enableStatus = computed(() => route.query.enableStatus ?? null)

const statsFormData = reactive({
  endDate: getTodayFormatDate(),
  startDate: getLastWeekFormatDate(),
  size: 10,
  current: 1
})

const shortcuts = [
  {
    text: '今天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 0)
      return [start, end]
    }
  },
  {
    text: '昨天',
    value: () => {
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 1)
      return [start, start]
    }
  },
  {
    text: '近七天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '近三十天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

const formatDateOnly = (dateTime) => {
  if (!dateTime) {
    return dateTime
  }
  return String(dateTime).split(' ')[0]
}
const getDefaultDateRange = () => [formatDateOnly(getLastWeekFormatDate()), formatDateOnly(getTodayFormatDate())]
const dateValue = ref(getDefaultDateRange())
const showPane = ref('访问数据')

const getImgUrl = (url) => url ?? defaultImg

const syncStatsRange = (dateList) => {
  if (!dateList) {
    statsFormData.endDate = getTodayFormatDate()
    statsFormData.startDate = getLastWeekFormatDate()
  } else {
    statsFormData.startDate = dateList?.[0] + ' 00:00:00'
    statsFormData.endDate = dateList?.[1] + ' 23:59:59'
  }
}

const fetchStatsSummary = async () => {
  if (isGroup.value) {
    return API.group.queryGroupStats({ ...statsFormData, fullShortUrl: fullShortUrl.value, gid: gid.value })
  }
  return API.smallLinkPage.queryLinkStats({
    ...statsFormData,
    fullShortUrl: fullShortUrl.value,
    gid: gid.value,
    enableStatus: enableStatus.value
  })
}

const fetchStatsTable = async () => {
  if (isGroup.value) {
    return API.group.queryGroupTable({ gid: gid.value, ...statsFormData })
  }
  return API.smallLinkPage.queryLinkTable({
    gid: gid.value,
    fullShortUrl: fullShortUrl.value,
    ...statsFormData,
    enableStatus: enableStatus.value
  })
}

const statsInfo = ref({})
const tableInfo = ref(null)
const totalNums = ref(0)

const loadStats = async () => {
  syncStatsRange(dateValue.value)
  const [summaryRes, tableRes] = await Promise.all([fetchStatsSummary(), fetchStatsTable()])
  statsInfo.value = summaryRes?.data?.data || {}
  tableInfo.value = tableRes
  totalNums.value = tableRes?.data?.data?.total || 0
}

const handlePageChange = async (page) => {
  statsFormData.current = page?.current ?? 1
  statsFormData.size = page?.size ?? 10
  const tableRes = await fetchStatsTable()
  tableInfo.value = tableRes
  totalNums.value = tableRes?.data?.data?.total || 0
}

const handleSizeChange = (size) => {
  handlePageChange({ current: 1, size })
}

const handleCurrentChange = (current) => {
  handlePageChange({ current, size: statsFormData.size })
}

watch(
  () => dateValue.value,
  () => {
    statsFormData.current = 1
    loadStats()
  }
)

const markSectionVisible = (key) => {
  if (!sectionVisible[key]) {
    sectionVisible[key] = true
  }
  if (key === 'line') {
    lineReady.value = true
    if (lineNeedsRender.value) {
      lineNeedsRender.value = false
      nextTick(() => {
        initLineChart()
      })
    }
  }
  if (key === 'map') {
    mapReady.value = true
    if (mapNeedsRender.value) {
      mapNeedsRender.value = false
      nextTick(() => {
        initMap()
      })
    }
  }
}

const setupSectionObserver = () => {
  if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
    Object.keys(sectionVisible).forEach((key) => {
      markSectionVisible(key)
    })
    return
  }
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) {
          return
        }
        const key = entry.target?.dataset?.section
        if (key) {
          markSectionVisible(key)
        }
        observer.unobserve(entry.target)
      })
    },
    { rootMargin: '120px' }
  )
  nextTick(() => {
    const sections = {
      line: lineSectionRef,
      map: mapSectionRef,
      hour: hourSectionRef,
      topIp: topIpSectionRef,
      week: weekSectionRef,
      os: osSectionRef,
      browser: browserSectionRef,
      userType: userTypeSectionRef,
      network: networkSectionRef,
      device: deviceSectionRef
    }
    Object.entries(sections).forEach(([key, refValue]) => {
      const el = refValue.value
      if (!el) {
        markSectionVisible(key)
        return
      }
      el.dataset.section = key
      observer.observe(el)
    })
  })
}

onMounted(() => {
  loadStats()
  setupSectionObserver()
})

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home/space')
  }
}

const loadEcharts = async () => {
  if (echartsRef) {
    return echartsRef
  }
  if (!echartsLoading) {
    echartsLoading = import('echarts').then((mod) => {
      echartsRef = mod?.default ?? mod
      return echartsRef
    })
  }
  return echartsLoading
}

const ensureMapModule = async (mapType) => {
  if (mapType === 'china') {
    if (!mapModuleLoaded.china) {
      await loadEcharts()
      await import('echarts/map/js/china.js')
      mapModuleLoaded.china = true
    }
    return
  }
  if (mapType === 'world') {
    if (!mapModuleLoaded.world) {
      await loadEcharts()
      await import('echarts/map/js/world.js')
      mapModuleLoaded.world = true
    }
  }
}

const getChartInstance = async (dom) => {
  if (!dom) {
    return null
  }
  const echarts = await loadEcharts()
  return echarts.getInstanceByDom(dom) || echarts.init(dom)
}

const resizeCharts = () => {
  if (!echartsRef) {
    return
  }
  const lineChartDom = document.querySelector('.lineChart')
  const chinaMapDom = document.querySelector('.chinaMap')
  const worldMapDom = document.querySelector('.worldMap')
  const lineInstance = lineChartDom ? echartsRef.getInstanceByDom(lineChartDom) : null
  const chinaInstance = chinaMapDom ? echartsRef.getInstanceByDom(chinaMapDom) : null
  const worldInstance = worldMapDom ? echartsRef.getInstanceByDom(worldMapDom) : null
  lineInstance?.resize()
  chinaInstance?.resize()
  worldInstance?.resize()
}

const requestLineRender = () => {
  lineNeedsRender.value = true
  if (lineReady.value) {
    lineNeedsRender.value = false
    nextTick(() => {
      initLineChart()
    })
  }
}

const requestMapRender = () => {
  mapNeedsRender.value = true
  if (mapReady.value) {
    mapNeedsRender.value = false
    nextTick(() => {
      initMap()
    })
  }
}

const dailyXAxis = ref()
const isLine = ref(true)
const initLineChart = async () => {
  const lineChartDom = document.querySelector('.lineChart')
  const lineChart = await getChartInstance(lineChartDom)
  if (!lineChart) {
    return
  }
  let option = {
    title: { show: false, text: 'Stacked Line' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['访问次数', '访问人数', '访问IP数'] },
    grid: { left: '3%', right: '9%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: dailyXAxis.value },
    yAxis: { type: 'value' },
    series: [
      { name: '访问次数', type: 'line', data: pvList.value },
      { name: '访问人数', type: 'line', data: uvList.value },
      { name: '访问IP数', type: 'line', data: uipList.value }
    ]
  }
  lineChart.setOption(option)
  lineChart.resize()
}

const totalPv = ref(0)
const pvList = ref([])
const totalUv = ref(0)
const uvList = ref([])
const totalUip = ref(0)
const uipList = ref([])
const visitsData = ref()

watch(
  () => statsInfo.value?.daily,
  () => {
    totalPv.value = 0
    totalUv.value = 0
    totalUip.value = 0
    pvList.value = []
    uvList.value = []
    uipList.value = []
    dailyXAxis.value = []
    visitsData.value = statsInfo.value?.daily
    visitsData?.value?.forEach((item) => {
      const { pv, uv, uip, date } = item
      const formDate = date.split('-')[1] + '月' + date.split('-')[2] + '日'
      totalPv.value += pv
      totalUv.value += uv
      totalUip.value += uip
      pvList.value.push(pv)
      uvList.value.push(uv)
      uipList.value.push(uip)
      dailyXAxis.value.push(formDate)
    })
    nextTick(() => {
      requestLineRender()
      if (lineReady.value) {
        resizeCharts()
      }
    })
  }
)

const getUrl1 = (img) => {
  if (img) {
    img = img.toLowerCase()
  }
  if (img?.includes('edge')) {
    return edge
  } else if (img?.includes('chrome')) {
    return Chorme
  } else if (img?.includes('fire')) {
    return firefox
  } else if (img?.includes('safari')) {
    return Safair
  } else if (img?.includes('wechat') || img?.includes('微信')) {
    return WeChat
  } else if (img?.includes('opera')) {
    return opera
  } else if (img?.includes('internet')) {
    return IE
  } else {
    return other
  }
}

const getUrl2 = (img) => {
  if (img) {
    img = img.toLowerCase()
  }
  if (img?.includes('android') || img?.includes('andriod')) {
    return Andriod
  } else if (img?.includes('ios')) {
    return iOS
  } else if (img?.includes('mac')) {
    return macOS
  } else if (img?.includes('windows')) {
    return Windows
  } else if (img?.includes('linux')) {
    return linux
  } else {
    return other
  }
}

const getUrl3 = (img) => {
  if (img) {
    img = img.toLowerCase()
  }
  if (img?.includes('pc')) {
    return PC
  } else {
    return Mobile
  }
}

const getUrl4 = (img) => {
  if (img) {
    img = img.toLowerCase()
  }
  if (img?.includes('mobile')) {
    return MobileDevices
  } else {
    return wifi
  }
}

const normalizeProvince = (rawLocale) => {
  if (!rawLocale) {
    return '未知'
  }
  let locale = String(rawLocale).trim()
  if (!locale || locale === '[]' || locale === 'null' || locale === 'undefined') {
    return '未知'
  }
  if (locale.indexOf('-') > -1) {
    const parts = locale.split('-')
    if (parts.length > 1 && parts[1]) {
      locale = parts[1]
    }
  }
  if (locale.indexOf(' ') > -1) {
    locale = locale.split(' ')[0]
  }
  const specialMap = {
    '内蒙古自治区': '内蒙古',
    '广西壮族自治区': '广西',
    '宁夏回族自治区': '宁夏',
    '新疆维吾尔自治区': '新疆',
    '西藏自治区': '西藏',
    '香港特别行政区': '香港',
    '澳门特别行政区': '澳门',
    '台湾省': '台湾'
  }
  if (specialMap[locale]) {
    return specialMap[locale]
  }
  locale = locale.replace(/省/g, '')
  locale = locale.replace(/市/g, '')
  locale = locale.replace(/自治区/g, '')
  locale = locale.replace(/特别行政区/g, '')
  locale = locale.replace(/壮族/g, '')
  locale = locale.replace(/回族/g, '')
  locale = locale.replace(/维吾尔/g, '')
  locale = locale.replace(/蒙古/g, '')
  return locale || '未知'
}

const chinaMapMode = ref('province')
const chinaMapData = ref([])
const chinaScatterData = ref([])
const chinaProvinceData = ref([])
const chinaTopLabels = ref([])
const chinaTotalNum = ref(0)
const chinaTopTitle = computed(() => (chinaMapMode.value === 'city' ? 'TOP 10 城市' : 'TOP 10 省份'))

const buildChinaMapData = () => {
  const cityStats = Array.isArray(statsInfo.value?.localeCnCityStats) ? statsInfo.value?.localeCnCityStats : []
  if (cityStats.length) {
    chinaMapMode.value = 'city'
    chinaTotalNum.value = 0
    const mapped = cityStats.map((item) => {
      const { cnt, city, province, ratio, lng, lat } = item || {}
      const count = Number(cnt) || 0
      chinaTotalNum.value += count
      const label = province ? `${province}-${city}` : city
      return {
        name: city || '未知',
        label: label || '未知',
        value: count,
        ratio: ratio || 0,
        province,
        lng,
        lat
      }
    })
    mapped.sort((a, b) => b.value - a.value)
    chinaMapData.value = mapped
    chinaTopLabels.value = mapped.slice(0, 5).map((item) => item.label)
    const provinceTotals = new Map()
    mapped.forEach((item) => {
      const normalized = normalizeProvince(item.province)
      if (!normalized || normalized === '未知') {
        return
      }
      provinceTotals.set(normalized, (provinceTotals.get(normalized) || 0) + item.value)
    })
    chinaProvinceData.value = Array.from(provinceTotals.entries()).map(([name, value]) => ({ name, value }))
    chinaScatterData.value = mapped
      .filter((item) => Number.isFinite(item.lng) && Number.isFinite(item.lat))
      .slice(0, 200)
      .map((item) => ({
        name: item.label,
        value: [item.lng, item.lat, item.value]
      }))
    return
  }
  chinaMapMode.value = 'province'
  chinaTotalNum.value = 0
  const localeStats = Array.isArray(statsInfo.value?.localeCnStats) ? statsInfo.value?.localeCnStats : []
  const mapped = localeStats.map((item) => {
    const { cnt, locale, ratio } = item || {}
    const normalized = normalizeProvince(locale)
    const count = Number(cnt) || 0
    chinaTotalNum.value += count
    return { name: normalized, label: normalized, value: count, ratio: ratio || 0 }
  })
  mapped.sort((a, b) => b.value - a.value)
  chinaMapData.value = mapped
  chinaProvinceData.value = mapped
  chinaTopLabels.value = []
  chinaScatterData.value = []
}

watch(
  () => [statsInfo.value?.localeCnCityStats, statsInfo.value?.localeCnStats],
  () => {
    buildChinaMapData()
    nextTick(() => {
      requestMapRender()
      if (mapReady.value) {
        resizeCharts()
      }
    })
  },
  { deep: true }
)

const worldMapData = ref([{ name: '中国', value: 0 }])
const isChina = ref(true)
const initChinaMap = async () => {
  const chinaMapDom = document.querySelector('.chinaMap')
  await ensureMapModule('china')
  const chinaMap = await getChartInstance(chinaMapDom)
  if (!chinaMap) {
    return
  }
  const isCityMode = chinaMapMode.value === 'city'
  const topLabelSet = new Set(chinaTopLabels.value)
  const option = isCityMode
    ? {
        tooltip: {
          formatter: function (params) {
            const val = Array.isArray(params.value) ? params.value[2] : params.value
            return `${params.name} : ${val || 0}`
          }
        },
        visualMap: {
          min: 0,
          max: Math.max(...chinaProvinceData.value.map((item) => item.value), 0),
          left: 'left',
          top: 'bottom',
          text: ['高', '低'],
          calculable: false,
          orient: 'horizontal',
          inRange: { color: ['#ffd7c2', '#ff7a45'] },
          seriesIndex: 0
        },
        geo: {
          map: 'china',
          roam: false,
          label: { show: false },
          itemStyle: { areaColor: '#f2f6ff', borderColor: '#d5e2ff' },
          emphasis: { itemStyle: { areaColor: '#78dffc' } }
        },
        series: [
          {
            type: 'map',
            map: 'china',
            geoIndex: 0,
            data: chinaProvinceData.value
          },
          {
            type: 'effectScatter',
            coordinateSystem: 'geo',
            data: chinaScatterData.value,
            symbolSize: (val) => Math.max(6, Math.min(22, Math.sqrt(val[2]) * 2)),
            rippleEffect: { scale: 3 },
            itemStyle: { color: '#ff7a45' },
            label: {
              show: true,
              color: '#ff7a45',
              formatter: (params) => (topLabelSet.has(params.name) ? params.name : ''),
              position: 'right'
            }
          }
        ]
      }
    : {
        tooltip: {
          formatter: function (params) {
            if (params.value) {
              return params.name + ' : ' + params.value
            } else {
              return params.name + ' : ' + '0'
            }
          }
        },
        visualMap: {
          min: 0,
          max: chinaTotalNum.value,
          left: 'left',
          top: 'bottom',
          text: ['高', '低'],
          calculable: false,
          orient: 'horizontal',
          inRange: { color: ['#e0ffff', '#006edd'], symbolSize: [30, 100] }
        },
        series: {
          type: 'map',
          map: 'china',
          itemStyle: { emphasis: { areaColor: '#78dffc' } },
          data: chinaMapData.value
        }
      }
  chinaMap.setOption(option)
  chinaMap.resize()
}

let nameMap = {}
const initWorldMap = async () => {
  const worldMapDom = document.querySelector('.worldMap')
  await ensureMapModule('world')
  const worldMap = await getChartInstance(worldMapDom)
  if (!worldMap) {
    return
  }
  const option = {
    tooltip: { formatter: '{b0}: {c0}' },
    nameMap,
    visualMap: {
      min: 0,
      max: 1000,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      calculable: false,
      orient: 'horizontal',
      inRange: { color: ['#e0ffff', '#006edd'], symbolSize: [30, 100] }
    },
    series: {
      name: '数量',
      type: 'map',
      map: 'world',
      itemStyle: { emphasis: { areaColor: '#78dffc' } },
      data: worldMapData.value
    }
  }
  worldMap.setOption(option)
  worldMap.resize()
}

const initMap = () => {
  if (isChina.value) {
    void initChinaMap()
    return
  }
  void initWorldMap()
}

const userTypeList = ref([0, 0])
const deviceList = ref([0, 0])
const netWorkList = ref([0, 0])

watch(
  () => statsInfo.value?.uvTypeStats,
  (list) => {
    let newCount = 0
    let oldCount = 0
    list?.forEach((item) => {
      if (item.uvType === 'newUser') {
        newCount = item.cnt
      } else if (item.uvType === 'oldUser') {
        oldCount = item.cnt
      }
    })
    userTypeList.value = [newCount, oldCount]
  },
  { immediate: true }
)

watch(
  () => statsInfo.value?.deviceStats,
  (list) => {
    let pcCount = 0
    let mobileCount = 0
    list?.forEach((item) => {
      if (item.device === 'Mobile') {
        mobileCount = item.cnt
      } else {
        pcCount = item.cnt
      }
    })
    deviceList.value = [pcCount, mobileCount]
  },
  { immediate: true }
)

watch(
  () => statsInfo.value?.networkStats,
  (list) => {
    let wifiCount = 0
    let mobileCount = 0
    list?.forEach((item) => {
      const network = String(item.network ?? item.device ?? '').toLowerCase()
      if (network.includes('mobile')) {
        mobileCount = item.cnt
      } else {
        wifiCount = item.cnt
      }
    })
    netWorkList.value = [wifiCount, mobileCount]
  },
  { immediate: true }
)

watch(
  () => showPane.value,
  (pane) => {
    if (pane === '访问数据') {
      nextTick(() => {
        requestLineRender()
        requestMapRender()
        if (lineReady.value || mapReady.value) {
          resizeCharts()
        }
      })
    }
  }
)
</script>

<style lang="less" scoped>
.stats-page {
  padding: 12px 12px 24px;
}

.stats-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.back-btn {
  align-self: flex-start;
}

.header-main {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.header-text .title {
  font-size: 18px;
  font-weight: 600;
  color: var(--ink, #0b1b2e);
}

.header-text .origin {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-soft, #6b7a90);
  word-break: break-all;
}

.group-count {
  font-size: 12px;
  color: var(--ink-soft, #6b7a90);
}

.stats-toolbar {
  margin: 12px 0;
}

.content-box {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  width: 100%;
}

.chart-item {
  height: 300px;
  min-width: 300px;
  margin: 10px;
}

.chinaMap,
.worldMap {
  width: 330px;
  height: 240px;
}

.list-chart {
  display: flex;
  justify-content: space-between;
}

.list-chart .top10 {
  padding: 15px 30px;
  width: 400px;
  height: 270px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.list-chart .top10 .top-item {
  display: flex;
  flex-direction: column;
  flex-wrap: wrap;
}

.list-chart .top10 .top-item div {
  height: 40px;
  display: flex;
  align-items: center;
  margin-right: 30px;
}

.list-chart .top10 span:nth-child(1) {
  color: #3464e0;
  font-size: 12px;
  font-weight: 700;
}

.list-chart .top10 .key-value {
  display: flex;
  justify-content: space-between;
  width: 150px;
}

.lineChart {
  margin: 10px;
  width: 600px;
  height: 200px;
}

.chart-placeholder {
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8aa0b6;
  font-size: 13px;
}

.flex-box {
  display: flex;
  justify-content: space-around;
}

.pagination-block .el-pagination {
  margin-left: 20%;
}

.scroll-box {
  height: calc(100vh - 280px);
  overflow: scroll;
}

@media (max-width: 768px) {
  .stats-page {
    padding: 10px 10px 20px;
  }

  .content-box {
    flex-direction: column;
    align-items: stretch;
  }

  .chart-item {
    width: 100% !important;
    min-width: 0;
    height: auto;
    margin: 10px 0;
  }

  .lineChart {
    width: 100% !important;
    height: 220px;
    margin: 0;
  }

  .chinaMap,
  .worldMap {
    width: 100%;
    height: 220px;
  }

  .list-chart {
    flex-direction: column;
    gap: 12px;
  }

  .list-chart .top10 {
    width: 100%;
    height: auto;
  }

  .pagination-block .el-pagination {
    margin-left: 0;
    justify-content: center;
  }

  .scroll-box {
    height: auto;
    max-height: none;
    overflow-y: visible;
    overflow-x: hidden;
  }
}
</style>
