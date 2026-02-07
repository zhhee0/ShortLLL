<template>
  <el-dialog class="charts-dialog" v-model="dialogVisible" :title="props.title" width="70%" :before-close="handleClose">
    <template #header>
      <div style="display: flex">
        <img v-if="!isGroup" :src="getImgUrl(props.favicon)" width="25" height="25" alt="" />
        <div style="display: flex; flex-direction: column; margin-left: 5px">
          <span style="font-size: 25px; line-height: 25px; font-weight: 550">{{
            props.title
          }}</span>
          <span v-if="!isGroup" style="margin-top: 5px; font-size: 15px">{{
            props.originUrl
          }}</span>
        </div>
      </div>
      <span v-if="isGroup" style="margin: 5px 0 0 5px">共{{ props.nums }}条短链接</span>
    </template>
    <div class="charts-toolbar">
      <el-date-picker
        v-model="dateValue"
        :clearable="true"
        type="daterange"
        range-separator="To"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD"
        :shortcuts="shortcuts"
        :size="size"
      />
    </div>
    <!-- 具体展示内容 -->
    <el-tabs v-model="showPane">
      <!-- 切换�?name用于确定展示哪个标签，和showPane对应 -->
      <el-tab-pane name="访问数据" label="访问数据">
        <!-- 数据图表 -->
        <div class="content-box scroll-box" style="height: calc(100vh - 280px); overflow: scroll">
          <!-- 访问曲线 -->
          <TitleContent class="chart-item" style="width: 800px" title="访问曲线" @onMounted="initLineChart">
            <template v-slot:titleButton>
              <div>
                <el-button @click="isLine = !isLine">切换为曲线</el-button>
              </div>
            </template>
            <template #content>
              <div class="list-chart">
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
                <!-- 折线�?-->
                <div v-show="isLine" class="lineChart"></div>
                <!-- 表格 -->
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
            </template>
          </TitleContent>
          <!-- 地图 -->
          <TitleContent class="chart-item" style="width: 800px" title="访问地区" @onMounted="initMap">
            <template #titleButton>
              <!-- <el-button @click="isChina = !isChina">切换为世界地图</el-button> -->
            </template>
            <template #content>
              <div class="list-chart">
                <div v-show="isChina" class="top10">
                  <span style="font-size: 14px">TOP 10 省份</span>
                  <div class="top-item" v-for="(item, index) in chinaMapData" :key="item.name">
                    <div v-if="index <= 9" class="key-value">
                      <span>{{ index + 1 + '. ' + item.name }}</span>
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
            </template>
          </TitleContent>
          <!-- 24小时分布 -->
          <TitleContent class="chart-item" title="24小时分布" style="width: 800px">
            <template #content>
              <BarChart style="height: 100%; width: 100%" :chartData="{
                xAxis: [
                  0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                  22, 23
                ],
                value: props.info?.hourStats || new Array(24).fill(0)
              }"></BarChart>
            </template>
          </TitleContent>
          <!-- 高频访问IP -->
          <TitleContent class="chart-item" title="高频访问IP" style="width: 390px">
            <template #content>
              <KeyValue :dataLists="props.info?.topIpStats" style="height: 100%; width: 100%"></KeyValue>
            </template>
          </TitleContent>
          <!-- 一周分�?-->
          <TitleContent class="chart-item" title="一周分布" style="width: 390px">
            <template #content>
              <BarChart style="height: 100%; width: 100%" :chartData="{
                xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
                value: props.info?.weekdayStats || new Array(7).fill(0)
              }"></BarChart>
            </template>
          </TitleContent>

          <!-- 访问来源TOP5 -->
          <!-- <TitleContent class="chart-item" title="访问来源 TOP5" style="width: 390px">
            <template #content>
              <KeyValue :data-lists="IPdataList" style="height: 100%; width: 100%;"></KeyValue>
            </template>
          </TitleContent> -->
          <!-- 操作系统 -->
          <TitleContent class="chart-item" title="操作系统" style="width: 390px">
            <template #content>
              <ProgressLine style="height: 100%; width: 100%" :dataLists="props.info?.osStats"></ProgressLine>
            </template>
          </TitleContent>
          <!-- 访问浏览器 -->
          <TitleContent class="chart-item" title="访问浏览器" style="width: 390px">
            <template #content>
              <ProgressLine style="height: 100%; width: 100%" :dataLists="props.info?.browserStats"></ProgressLine>
            </template>
          </TitleContent>
          <!-- 访客类型 -->
          <TitleContent v-if="!isGroup" class="chart-item" title="访客类型" style="width: 390px">
            <template #content>
              <ProgressPie style="height: 100%; width: 100%" :labels="['新访客', '旧访客']" :data="userTypeList"></ProgressPie>
            </template>
          </TitleContent>
          <!-- 访问网络 -->
          <TitleContent class="chart-item" title="访问网络" style="width: 390px">
            <template #content>
              <ProgressPie style="height: 100%; width: 100%" :labels="['WIFI', '移动数据']" :data="netWorkList"></ProgressPie>
            </template>
          </TitleContent>
          <!-- 访问设备 -->
          <TitleContent class="chart-item" title="访问设备" style="width: 390px">
            <template #content>
              <ProgressPie style="height: 100%; width: 100%" :labels="['电脑', '移动设备']" :data="deviceList"></ProgressPie>
            </template>
          </TitleContent>
        </div>
      </el-tab-pane>
      <el-tab-pane name="历史记录" label="历史记录">
        <el-table :data="tableInfo?.data?.data?.records" style="width: 100%; height: calc(100vh - 300px)">
          <el-table-column prop="createTime" label="访问时间" width="160" />
          <el-table-column prop="ip" label="访问IP" width="140" />
          <el-table-column prop="locale" label="访客地区"> </el-table-column>
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
        <!-- 分页�?-->
        <div class="pagination-block">
          <el-pagination v-model:current-page="pageParams.current" v-model:page-size="pageParams.size"
            :page-sizes="[10, 15, 20, 30]" layout="total, sizes, prev, pager, next, jumper" :total="totalNums"
            @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, nextTick } from 'vue'
import TitleContent from './TitleContent.vue'
import * as echarts from 'echarts'
import 'echarts/map/js/china.js'
import 'echarts/map/js/world.js'
import BarChart from './BarChart.vue'
import KeyValue from './KeyValue.vue'
import ProgressLine from './ProgressLine.vue'
import ProgressPie from './ProgressPie.vue'
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

// 选择时间
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
const getImgUrl = (url) => {
  return url ?? defaultImg
}
const dailyXAxis = ref()
// const dailyXAxis = ref([])
const showPane = ref('访问数据')
const dialogVisible = ref(false)
// 浏览�?
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
  }
  else if (img?.includes('internet')) {
    return IE
  }
  else {
    return other
  }
}
// 操作系统
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
// 访问设备（pc或者移动设备）
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
// 访问网络（wifi和移动网络）
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
const formatDateOnly = (dateTime) => {
  if (!dateTime) {
    return dateTime
  }
  return String(dateTime).split(' ')[0]
}
const getDefaultDateRange = () => [formatDateOnly(getLastWeekFormatDate()), formatDateOnly(getTodayFormatDate())]
const dateValue = ref(getDefaultDateRange())
const emit = defineEmits(['changeTime', 'changePage', 'paneChange'])
watch(
  () => dateValue.value,
  (newValue) => {
    if (!dialogVisible.value) {
      return
    }
    emit('changeTime', newValue, showPane.value)
  }
)
const props = defineProps({
  title: {
    type: String,
    default: '默认标题'
  },
  info: Object,
  tableInfo: Object,
  isGroup: Boolean,
  nums: Number,
  favicon: String,
  originUrl: String
})
const pageParams = reactive({
  current: 1,
  size: 10
})
const totalNums = ref(0)
watch(
  () => props.tableInfo,
  () => {
    totalNums.value = props?.tableInfo?.data?.data?.total
  }
)
watch(
  () => pageParams,
  (newValue) => {
    if (!dialogVisible.value || showPane.value !== '历史记录') {
      return
    }
    emit('changePage', newValue, showPane.value)
  },
  {
    deep: true
  }
)
watch(
  () => showPane.value,
  (pane) => {
    if (!dialogVisible.value) {
      return
    }
    emit('paneChange', pane)
    if (pane === '访问数据') {
      nextTick(() => {
        initLineChart()
        initMap()
        resizeCharts()
      })
    }
  }
)
watch(
  () => dialogVisible.value,
  (visible) => {
    if (!visible) {
      return
    }
    nextTick(() => {
      initLineChart()
      initMap()
      resizeCharts()
    })
  }
)
// const title = ref(props.title)
// const info = ref(props.info)

// watch(
//   () => {
//     props.info
//   },
//   () => {
//     console.log(props.info, props.title)
//     info.value = props.info
//     title.value = props.title
//   },
//   {
//     deep: true,
//   }
// )
const resizeCharts = () => {
  const lineChartDom = document.querySelector('.lineChart')
  const chinaMapDom = document.querySelector('.chinaMap')
  const worldMapDom = document.querySelector('.worldMap')
  const lineInstance = lineChartDom ? echarts.getInstanceByDom(lineChartDom) : null
  const chinaInstance = chinaMapDom ? echarts.getInstanceByDom(chinaMapDom) : null
  const worldInstance = worldMapDom ? echarts.getInstanceByDom(worldMapDom) : null
  lineInstance?.resize()
  chinaInstance?.resize()
  worldInstance?.resize()
}
const handleClose = () => {
  dateValue.value = null
  unVisible()
  showPane.value = '访问数据'
  pageParams.current = 1
  pageParams.size = 10
  dateValue.value = getDefaultDateRange()
  const scrollBox = document.querySelector('.scroll-box')
  if (scrollBox) {
    scrollBox.scrollTop = 0
  }
}
const isVisible = () => {
  dialogVisible.value = true
}
const unVisible = () => {
  dialogVisible.value = false
}
defineExpose({
  unVisible,
  isVisible
})

const normalizeProvince = (rawLocale) => {
  if (!rawLocale) {
    return "\u672a\u77e5"
  }
  let locale = String(rawLocale).trim()
  if (!locale || locale === "[]" || locale === "null" || locale === "undefined") {
    return "\u672a\u77e5"
  }
  if (locale.indexOf("-") > -1) {
    const parts = locale.split("-")
    if (parts.length > 1 && parts[1]) {
      locale = parts[1]
    }
  }
  if (locale.indexOf(" ") > -1) {
    locale = locale.split(" ")[0]
  }
  const specialMap = {
    "\u5185\u8499\u53e4\u81ea\u6cbb\u533a": "\u5185\u8499\u53e4",
    "\u5e7f\u897f\u58ee\u65cf\u81ea\u6cbb\u533a": "\u5e7f\u897f",
    "\u5b81\u590f\u56de\u65cf\u81ea\u6cbb\u533a": "\u5b81\u590f",
    "\u65b0\u7586\u7ef4\u543e\u5c14\u81ea\u6cbb\u533a": "\u65b0\u7586",
    "\u897f\u85cf\u81ea\u6cbb\u533a": "\u897f\u85cf",
    "\u9999\u6e2f\u7279\u522b\u884c\u653f\u533a": "\u9999\u6e2f",
    "\u6fb3\u95e8\u7279\u522b\u884c\u653f\u533a": "\u6fb3\u95e8",
    "\u53f0\u6e7e\u7701": "\u53f0\u6e7e"
  }
  if (specialMap[locale]) {
    return specialMap[locale]
  }
  locale = locale.replace(/\u7701/g, "")
  locale = locale.replace(/\u5e02/g, "")
  locale = locale.replace(/\u81ea\u6cbb\u533a/g, "")
  locale = locale.replace(/\u7279\u522b\u884c\u653f\u533a/g, "")
  locale = locale.replace(/\u58ee\u65cf/g, "")
  locale = locale.replace(/\u56de\u65cf/g, "")
  locale = locale.replace(/\u7ef4\u543e\u5c14/g, "")
  locale = locale.replace(/\u8499\u53e4/g, "")
  return locale || "\u672a\u77e5"
}
// 中国地图中展示的数据
const chinaMapData = ref([
  // {
  //   name: '北京',
  //   value: 0
  // }
])
// 中国地图中的总次�?
const chinaTotalNum = ref(0)
// 将请求到的数据转化为中国地图中需要的数据结构
watch(
  () => props.info?.localeCnStats,
  () => {
    chinaTotalNum.value = 0
    const localeStats = Array.isArray(props.info?.localeCnStats) ? props.info?.localeCnStats : []
    chinaMapData.value = localeStats.map((item) => {
      const { cnt, locale, ratio } = item || {}
      const normalized = normalizeProvince(locale)
      const count = Number(cnt) || 0
      chinaTotalNum.value += count
      return { name: normalized, value: count, ratio: ratio || 0 }
    })
    initChinaMap()
  },
  {
    deep: true
  }
)
// 世界地图中展示的数据
const worldMapData = ref([
  {
    name: '中国',
    value: 28397.812
  }
])
const isChina = ref(true)
const getChartInstance = (dom) => {
  if (!dom) {
    return null
  }
  return echarts.getInstanceByDom(dom) || echarts.init(dom)
}
const initChinaMap = () => {
  // 中国地图
  const chinaMapDom = document.querySelector('.chinaMap')
  const chinaMap = getChartInstance(chinaMapDom)
  if (!chinaMap) {
    return
  }
  const option = {
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
      inRange: {
        // 最小值到最大值的颜色
        color: ['#e0ffff', '#006edd'],
        symbolSize: [30, 100]
      }
    },
    series: {
      type: 'map',
      map: 'china',
      itemStyle: {
        // 鼠标移入后的样式
        emphasis: {
          // 鼠标移入后的颜色
          areaColor: '#78dffc'
        }
      },
      data: chinaMapData.value
    }
  }
  chinaMap.setOption(option)
  chinaMap.resize()
}
// 世界地图默认展示英文国家名，在echarts的options中配置nameMap进行转化
let nameMap = {}
const initWorldMap = () => {
  // 世界地图
  const worldMapDom = document.querySelector('.worldMap')
  const worldMap = getChartInstance(worldMapDom)
  if (!worldMap) {
    return
  }
  const option = {
    tooltip: {
      formatter: '{b0}: {c0}'
    },
    nameMap,
    visualMap: {
      min: 0,
      max: 1000,
      left: 'left',
      top: 'bottom',
            text: ['高', '低'],
      calculable: false,
      orient: 'horizontal',
      inRange: {
        // 最小值到最大值的颜色
        color: ['#e0ffff', '#006edd'],
        symbolSize: [30, 100]
      }
    },
    series: {
      name: '数量',
      type: 'map',
      map: 'world',
      itemStyle: {
        // 鼠标移入后的样式
        emphasis: {
          // 鼠标移入后的颜色
          areaColor: '#78dffc'
        }
      },
      data: worldMapData.value
    }
  }
  worldMap.setOption(option)
  worldMap.resize()
}
const initMap = () => {
  initChinaMap()
  initWorldMap()
}
// 访问次数相关
const isLine = ref(true)
// 访问曲线数据请求
const initLineChart = () => {
  const lineChartDom = document.querySelector('.lineChart')
  const lineChart = getChartInstance(lineChartDom)
  if (!lineChart) {
    return
  }
  let option = {
    title: {
      show: false,
      text: 'Stacked Line'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['访问次数', '访问人数', '访问IP数']
    },
    grid: {
      left: '3%',
      right: '9%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dailyXAxis.value
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问次数',
        type: 'line',
        data: pvList.value
      },
      {
        name: '访问人数',
        type: 'line',
        data: uvList.value
      },
      {
        name: '访问IP数',
        type: 'line',
        data: uipList.value
      }
    ]
  }
  lineChart.setOption(option)
  lineChart.resize()
}
// 总访问次�?
const totalPv = ref(0)
// 访问次数数据集数�?
const pvList = ref([])
// 总访问人�?
const totalUv = ref(0)
// 访问人数数据集数�?
const uvList = ref([])
// 总访问IP�?
const totalUip = ref(0)
// 访问IP数数据集数组
const uipList = ref([])
// 更新数据
watch(
  () => props?.info?.daily,
  () => {
    // 归零
    totalPv.value = 0
    totalUv.value = 0
    totalUip.value = 0
    pvList.value = []
    uvList.value = []
    uipList.value = []
    dailyXAxis.value = []
    visitsData.value = props?.info?.daily
    // 获取总数量和数据集数�?
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
    initLineChart()
  }
)
// 访问表格数据
const visitsData = ref()
// 访问设备和访问网�?
const userTypeList = ref([0, 0])
const deviceList = ref([0, 0])
const netWorkList = ref([0, 0])
watch(
  () => props.info?.uvTypeStats,
  (list) => {
    let newCount = 0
    let oldCount = 0
    // 对访问用户类型的数据进行转化
    list?.forEach((item) => {
      if (item.uvType === 'newUser') {
        newCount = item.cnt
      } else if (item.uvType === 'oldUser') {
        oldCount = item.cnt
      }
    })
    userTypeList.value = [newCount, oldCount]
  },
  {
    immediate: true
  }
)
watch(
  () => props.info?.deviceStats,
  (list) => {
    let pcCount = 0
    let mobileCount = 0
    // 对访问设备类型的数据进行转化
    list?.forEach((item) => {
      if (item.device === 'Mobile') {
        mobileCount = item.cnt
      } else {
        pcCount = item.cnt
      }
    })
    deviceList.value = [pcCount, mobileCount]
  },
  {
    immediate: true
  }
)
watch(
  () => props.info?.networkStats,
  (list) => {
    let wifiCount = 0
    let mobileCount = 0
    // 对访问设备类型的数据进行转化
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
  {
    immediate: true
  }
)
</script>

<style lang="less" scoped>
.content-box {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  width: 100%;

  .chart-item {
    height: 300px;
    min-width: 300px;
    margin: 10px;
  }
}

.chinaMap {
  width: 330px;
  height: 240px;
}

.worldMap {
  width: 330px;
  height: 240px;
}

.list-chart {
  display: flex;
  justify-content: space-between;

  .top10 {
    padding: 15px 30px;
    width: 400px;
    height: 270px;
    display: flex;
    flex-direction: column;
    overflow-y: auto;

    .top-item {
      display: flex;
      flex-direction: column;
      flex-wrap: wrap;

      div {
        height: 40px;
        display: flex;
        align-items: center;
        margin-right: 30px;
      }
    }

    span:nth-child(1) {
      color: #3464e0;
      font-size: 12px;
      font-weight: 700;
    }

    .key-value {
      display: flex;
      justify-content: space-between;
      width: 150px;
    }
  }
}

.lineChart {
  margin: 10px;
  width: 600px;
  height: 200px;
}

.flex-box {
  display: flex;
  justify-content: space-around;
}

.pagination-block {
  .el-pagination {
    margin-left: 20%;
  }
}

.charts-toolbar {
  position: absolute;
  right: 30px;
  z-index: 999;
}

@media (max-width: 768px) {
  :deep(.charts-dialog) {
    width: 96vw !important;
    margin-top: 12px;
  }

  :deep(.charts-dialog .el-dialog__body) {
    padding: 16px 14px;
  }

  .charts-toolbar {
    position: static;
    margin: 8px 0 12px;
  }

  .content-box {
    flex-direction: column;
  }

  .content-box .chart-item {
    width: 100% !important;
    min-width: 0;
    height: auto;
  }

  .lineChart {
    width: 100% !important;
    height: 220px;
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

  .pagination-block {
    .el-pagination {
      margin-left: 0;
      justify-content: center;
    }
  }

  .scroll-box {
    height: auto !important;
    max-height: calc(100vh - 220px);
    overflow-y: auto;
  }
}
</style>







