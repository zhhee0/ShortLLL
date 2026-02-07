<template>
  <div class="my-space-page" :style="backgroundStyle">
    <div class="options-box">
      <div class="option-title flex-box">
        <div>
          短链分组<span> 共{{ editableTabs?.length }}组</span>
        </div>
        <div class="hover-box" style="width: 24px" @click="showAddGroup">
          <img src="@/assets/svg/添加.svg" alt="" />
        </div>
      </div>
      <!-- 拖动选项 -->
      <ul class="sortOptions">
        <li v-for="(item, index) in editableTabs" :key="item.gid">
          <div class="item-box flex-box hover-box" :class="{ selectedItem: selectedIndex === index }"
            @click="changeSelectIndex(index)">
            <div style="display: flex">
              <img class="drag-handle" src="@/assets/svg/移动竖.svg" width="13" style="margin-right: 3px" alt="" />
              <span class="over-text">{{ item.name }}</span>
            </div>
            <div class="flex-box">
              <!-- 图标 -->
              <el-tooltip show-after="500" class="box-item" effect="dark" :content="'查看图表'" placement="bottom-end">
                <!-- 传group是为了表示这个请求是查询分组图表数据 -->
                <el-icon v-if="!(item.shortLinkCount === 0 || item.shortLinkCount === null)" class="edit"
                  :class="{ zero: item.shortLinkCount === 0 }"
                  @click="chartsVisible({ describe: item.name, gid: item.gid, group: true })">
                  <Histogram />
                </el-icon>
              </el-tooltip>
              <!-- 编辑按钮 -->
              <el-dropdown>
                <div class="block">
                  <el-icon class="edit" v-if="item.title !== '默认分组'">
                    <Tools />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="showEditGroup(item.gid, item.name)">编辑</el-dropdown-item>
                    <el-dropdown-item @click="deleteGroup(item.gid)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <span class="item-length">{{ item.shortLinkCount ?? 0 }}</span>
            </div>
          </div>
        </li>
      </ul>
      <div class="recycle-bin">
        <!-- 当selectIndex等于-1时代表选中的是回收站 -->
        <div class="recycle-box hover-box" :class="{ selectedItem: selectedIndex === -1 }" @click="recycleBin">
          回收站
          <el-icon style="margin-left: 5px; font-size: 20px">
            <Delete />
          </el-icon>
        </div>
      </div>
    </div>
    <!-- 主要数据展示区域 -->
    <div class="content-box">
      <div class="table-box" :class="{ 'is-recycle': isRecycleBin }">
        <!-- 默认展示创建短链输入框和按钮 -->
        <div v-if="!isRecycleBin" class="buttons-box">
          <div style="width: 100%; display: flex">
            <!-- <el-input style="flex: 1; margin-right: 20px" placeholder="请输入http://或https://开头的连接或引用跳转程序"></el-input> -->
            <el-button class="addButton" type="primary" style="width: 130px; margin-right: 10px"
              @click="isAddSmallLink = true">创建短链</el-button>
            <el-button style="width: 130px; margin-right: 10px" @click="isAddSmallLinks = true">批量创建</el-button>
            <el-popconfirm width="260" title="确认批量删除？将移入回收站" @confirm="batchToRecycleBin">
              <template #reference>
                <el-button type="danger" :disabled="selectedRows.length === 0">批量删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
        <!-- 展示回收站信息 -->
        <div v-else class="recycle-bin-box">
          <div class="recycle-info">
            <span>回收站</span>
            <span>共{{ recycleBinNums }}条短链接</span>
          </div>
          <div class="recycle-actions">
            <el-popconfirm width="260" title="确认批量恢复？" @confirm="batchRecoverLink">
              <template #reference>
                <el-button type="primary" :disabled="selectedRows.length === 0">批量恢复</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm width="280" title="确认批量删除？删除后短链跳转失效，停止统计" @confirm="batchRemoveLink">
              <template #reference>
                <el-button type="danger" :disabled="selectedRows.length === 0">批量删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
        <!-- 表格展示区域 -->
        <el-table v-if="!isMobile" class="myspace-table" ref="tableRef" :data="tableData" :height="isMobile ? null : 'calc(100vh - 240px)'" style="width: calc(100vw - 230px)"
          :header-cell-style="{ background: 'rgba(255, 255, 255, 0.9)', color: '#0b1b2e' }" @selection-change="handleSelectionChange">
          <!-- 数据为空时展示的内容 -->
          <template #empty>
            <div style="height: 60vh; display: flex; align-items: center; justify-content: center">
              暂无链接
            </div>
          </template>
          <el-table-column type="selection" width="35" />
          <el-table-column label="短链接信息" prop="info" min-width="300">
            <template #header>
              <el-dropdown>
                <div :class="{ orderIndex: orderIndex === 0 }" class="block" style="margin-top: 3px">
                  <span>短链接信息</span>
                  <el-icon>
                    <CaretBottom />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-item @click="pageParams.orderTag = null, orderIndex = 0">创建时间</el-dropdown-item>
                </template>
              </el-dropdown>
            </template>
            <template #default="scope">
              <div class="table-link-box" :class="{
                isExpire: scope?.row?.validDateType === 1 && !isExpire(scope?.row?.validDate)
              }">
                <img :src="getImgUrl(scope.row.favicon)" :key="scope?.row?.id" width="20" height="20" alt="" />
                <div class="name-date">
                  <el-tooltip show-after="500" :content="scope.row.describe">
                    <span>{{ scope.row.describe }}</span>
                  </el-tooltip>
                  <div class="time" style="display: flex">
                    <span>{{ scope.row.createTime }}</span>
                    <el-tooltip show-after="500" v-if="scope?.row?.validDate" :content="'到期时间：' + scope?.row?.validDate">
                      <img v-if="isExpire(scope?.row?.validDate)" width="18" height="18" src="@/assets/png/沙漏倒计时.png"
                        alt="" />
                      <div v-else><span>已失效</span></div>
                    </el-tooltip>
                  </div>
                  <div v-if="isMobile" class="mobile-url">
                    <el-link type="primary" :underline="false" target="_blank"
                      :disabled="scope?.row?.validDateType === 1 && !isExpire(scope?.row?.validDate)"
                      :href="'http://' + scope.row.fullShortUrl">{{ scope.row.domain + '/' + scope.row.shortUri }}</el-link>
                    <el-tooltip show-after="500" :content="scope.row.originUrl">
                      <span class="origin-url">{{ scope.row.originUrl }}</span>
                    </el-tooltip>
                  </div>
                  <div v-if="isMobile" class="mobile-stats">
                    <div class="stat-item">
                      <span class="label">PV(访问次数)</span>
                      <span class="value">{{ scope.row.todayPv }}/{{ scope.row.totalPv }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="label">UV(访客数)</span>
                      <span class="value">{{ scope.row.todayUv }}/{{ scope.row.totalUv }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="label">IP(独立IP)</span>
                      <span class="value">{{ scope.row.todayUip }}/{{ scope.row.totalUip }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" label="短链接网址" prop="url" min-width="300">
            <template #default="scope">
              <div class="table-url-box">
                <!-- 当失效后就不能在点击跳转了 -->
                <el-link type="primary" :underline="false" target="_blank"
                  :disabled="scope?.row?.validDateType === 1 && !isExpire(scope?.row?.validDate)"
                  :href="'http://' + scope.row.fullShortUrl">{{ scope.row.domain + '/' + scope.row.shortUri }}</el-link>
                <el-tooltip show-after="500" :content="scope.row.originUrl">
                  <span>{{ scope.row.originUrl }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="copy" :width="isMobile ? 130 : 170">
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <!-- 二维码 -->
                <QRCode :url="'http://' + scope.row.fullShortUrl"></QRCode>
                <!-- 表格中的复制链接按钮 -->
                <el-tooltip show-after="500" class="box-item" effect="dark" content="复制链接" placement="bottom-end">
                  <el-icon @click="copyUrl('http://' + scope.row.fullShortUrl)" class="table-edit copy-url">
                    <Share />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" label="访问次数" prop="times" width="120">
            <template #header>
              <el-dropdown>
                <div :class="{ orderIndex: orderIndex === 1 }" class="block" style="margin-top: 3px">
                  <span>访问次数</span>
                  <el-icon>
                    <CaretBottom />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-item @click="pageParams.orderTag = 'todayPv', orderIndex = 1">今日访问次数</el-dropdown-item>
                  <el-dropdown-item @click="pageParams.orderTag = 'totalPv', orderIndex = 1">累计访问次数</el-dropdown-item>
                </template>
              </el-dropdown>
            </template>
            <template #default="scope">
              <div class="times-box">
                <div class="today-box">
                  <span>今日</span>
                  <span>{{ scope.row.todayPv }}</span>
                </div>
                <div class="total-box">
                  <span>累计</span>
                  <span>{{ scope.row.totalPv }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" label="访问人数" prop="people" width="120">
            <template #header>
              <el-dropdown>
                <div :class="{ orderIndex: orderIndex === 2 }" class="block" style="margin-top: 3px">
                  <span>访问人数</span>
                  <el-icon>
                    <CaretBottom />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-item @click="pageParams.orderTag = 'todayUv', orderIndex = 2">今日访问人数</el-dropdown-item>
                  <el-dropdown-item @click="pageParams.orderTag = 'totalUv', orderIndex = 2">累计访问人数</el-dropdown-item>
                </template>
              </el-dropdown>
            </template>
            <template #default="scope">
              <div class="times-box">
                <div class="today-box">
                  <span>今日</span>
                  <span>{{ scope.row.todayUv }}</span>
                </div>
                <div class="total-box">
                  <span>累计</span>
                  <span>{{ scope.row.totalUv }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" label="IP数" prop="IP" width="120">
            <template #header>
              <el-dropdown>
                <div :class="{ orderIndex: orderIndex === 3 }" class="block" style="margin-top: 3px">
                  <span>IP数</span>
                  <el-icon>
                    <CaretBottom />
                  </el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-item @click="pageParams.orderTag = 'todayUip', orderIndex = 3">今日IP数</el-dropdown-item>
                  <el-dropdown-item @click="pageParams.orderTag = 'totalUip', orderIndex = 3">累计IP数</el-dropdown-item>
                </template>
              </el-dropdown>
            </template>
            <template #default="scope">
              <div class="times-box">
                <div class="today-box">
                  <span>今日</span>
                  <span>{{ scope.row.todayUip }}</span>
                </div>
                <div class="total-box">
                  <span>累计</span>
                  <span>{{ scope.row.totalUip }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column :fixed="isMobile ? false : 'right'" label="操作" :width="isMobile ? 140 : 180">
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <!-- <el-link
                :underline="false"
                class="el-link"
                type="primary"
                @click="chartsVisible(row?.info)"
                >数据</el-link
              >
              <el-link :underline="false" class="el-link" type="primary">编辑</el-link> -->
                <!-- 表格中的数据按钮 -->
                <el-tooltip show-after="500" class="box-item" effect="dark" content="查看图表" placement="bottom-end">
                  <el-icon class="table-edit" @click="chartsVisible(scope.row)">
                    <Histogram />
                  </el-icon>
                </el-tooltip>
                <!-- 正常页面展示编辑和删除 -->
                <template v-if="selectedIndex !== -1">
                  <!-- 表格中的编辑按钮 -->
                  <el-tooltip show-after="500" class="box-item" effect="dark" content="编辑" placement="bottom-end">
                    <el-icon @click="editLink(scope.row)" class="table-edit">
                      <Tools />
                    </el-icon>
                  </el-tooltip>
                  <!-- 删除按钮 -->
                  <el-tooltip show-after="500" class="box-item" effect="dark" content="删除" placement="bottom-end">
                    <el-popconfirm width="100" title="是否移入回收站" @confirm="toRecycleBin(scope.row)">
                      <template #reference>
                        <el-icon class="table-edit">
                          <Delete />
                        </el-icon>
                      </template>
                    </el-popconfirm>
                  </el-tooltip>
                </template>
                <!-- 回收站操作 -->
                <template v-else>
                  <!-- 回收站中的恢复按钮 -->
                  <el-tooltip show-after="500" class="box-item" effect="dark" content="恢复" placement="bottom-end">
                    <el-icon @click="recoverLink(scope.row)" class="table-edit">
                      <HelpFilled />
                    </el-icon>
                  </el-tooltip>
                  <!-- 回收站中的删除按钮 -->
                  <el-tooltip show-after="500" class="box-item" effect="dark" content="删除" placement="bottom-end">
                    <el-popconfirm width="300" title="删除后短链跳转会失效，同时停止数据统计，这是一个不可逆的操作，是否删除?"
                      @confirm="removeLink(scope.row)">
                      <template #reference>
                        <el-icon class="table-edit">
                          <Delete />
                        </el-icon>
                      </template>
                    </el-popconfirm>
                  </el-tooltip>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <!-- 卡片展示区域（移动端） -->
        <div v-else class="card-list">
          <div v-if="!tableData || tableData.length === 0" class="card-empty">暂无链接</div>
          <div v-for="row in tableData" :key="row.id || row.fullShortUrl" class="link-card"
            :class="{ expired: row.validDateType === 1 && !isExpire(row.validDate) }">
            <div class="card-header">
              <el-checkbox class="card-select" :model-value="isRowSelected(row)" @change="() => toggleRow(row)" />
              <img :src="getImgUrl(row.favicon)" width="22" height="22" alt="" />
              <div class="card-title">
                <div class="title">{{ row.describe }}</div>
                <div class="time">
                  <span>{{ row.createTime }}</span>
                  <el-tooltip show-after="500" v-if="row.validDate" :content="'到期时间：' + row.validDate">
                    <img v-if="isExpire(row.validDate)" width="18" height="18" src="@/assets/png/沙漏倒计时.png" alt="" />
                    <div v-else class="expire-tag"><span>已失效</span></div>
                  </el-tooltip>
                </div>
              </div>
            </div>
            <div class="card-url">
              <el-link type="primary" :underline="false" target="_blank"
                :disabled="row.validDateType === 1 && !isExpire(row.validDate)"
                :href="'http://' + row.fullShortUrl">{{ row.domain + '/' + row.shortUri }}</el-link>
              <div class="origin">{{ row.originUrl }}</div>
            </div>
            <div class="card-stats">
              <div class="stat-card">
                <div class="stat-label">PV(访问次数)</div>
                <div class="stat-values">
                  <span>今日 {{ row.todayPv }}</span>
                  <span>累计 {{ row.totalPv }}</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-label">UV(访客数)</div>
                <div class="stat-values">
                  <span>今日 {{ row.todayUv }}</span>
                  <span>累计 {{ row.totalUv }}</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-label">IP(独立IP)</div>
                <div class="stat-values">
                  <span>今日 {{ row.todayUip }}</span>
                  <span>累计 {{ row.totalUip }}</span>
                </div>
              </div>
            </div>
            <div class="card-tools">
              <el-button class="tool-btn" size="large" @click="copyUrl('http://' + row.fullShortUrl)">复制链接</el-button>
              <div class="qr-tool">
                <QRCode :url="'http://' + row.fullShortUrl"></QRCode>
                <span>二维码</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button class="action-btn" size="large" type="primary" @click="chartsVisible(row)">统计</el-button>
              <template v-if="selectedIndex !== -1">
                <el-button class="action-btn" size="large" @click="editLink(row)">编辑</el-button>
                <el-popconfirm width="200" title="是否移入回收站" @confirm="toRecycleBin(row)">
                  <template #reference>
                    <el-button class="action-btn" size="large" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
              <template v-else>
                <el-button class="action-btn" size="large" @click="recoverLink(row)">恢复</el-button>
                <el-popconfirm width="260" title="删除后短链跳转会失效，同时停止数据统计，是否删除?" @confirm="removeLink(row)">
                  <template #reference>
                    <el-button class="action-btn" size="large" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </div>
          </div>
        </div>
        <!-- 分页器 -->
        <div class="pagination-block">
          <el-pagination v-model:current-page="pageParams.current" v-model:page-size="pageParams.size"
            :page-sizes="[10, 15, 20, 30]" :layout="paginationLayout" :pager-count="isMobile ? 5 : 7" :small="isMobile" :total="totalNums"
            @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
      </div>
    </div>
    <!-- 新建分组弹框 -->
    <el-dialog v-model="isAddGroup" title="新建短链接分组" style="width: 40%">
      <el-form :model="form">
        <el-form-item label="分组名称：" :label-width="formLabelWidth">
          <el-input autocomplete="off" v-model="newGroupName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isAddGroup = false">取消</el-button>
          <el-button type="primary" @click="addGroup" :disabled="addGroupLoading"> 确认 </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 编辑分组弹框 -->
    <el-dialog v-model="isEditGroup" title="编辑短链接分组" style="width: 40%">
      <el-form :model="form">
        <el-form-item label="分组名称：" :label-width="formLabelWidth">
          <el-input autocomplete="off" v-model="editGroupName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isEditGroup = false">取消</el-button>
          <el-button type="primary" @click="editGroup" :disabled="editGroupLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 创建短链弹框 -->
    <el-dialog @close="afterAddLink" v-model="isAddSmallLink" title="创建链接">
      <el-tabs class="demo-tabs">
        <el-tab-pane>
          <template #label>
            <span class="custom-tabs-label">
              <el-icon>
                <Link />
              </el-icon>
              <span>普通跳转</span>
            </span>
          </template>
          <CreateLink ref="createLink1Ref" :groupInfo="editableTabs" @onSubmit="addLink" @cancel="cancelAddLink"
            :defaultGid="pageParams.gid" :is-single="true"></CreateLink>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    <!-- 修改短链信息弹框 -->
    <el-dialog @close="afterAddLink" v-model="isEditLink" title="编辑链接">
      <EditLink ref="editLinkRef" :editData="editData" :groupInfo="editableTabs" @onSubmit="coverEditLink"
        @updatePage="updatePage" @cancel="coverEditLink"></EditLink>
    </el-dialog>
    <!-- 批量创建短链弹框 -->
    <el-dialog @close="afterAddLink" v-model="isAddSmallLinks" title="批量链接">
      <CreateLinks ref="createLink2Ref" :groupInfo="editableTabs" @onSubmit="addLink" @cancel="cancelAddLink"
        :defaultGid="pageParams.gid"></CreateLinks>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, getCurrentInstance, watch, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import Sortable from 'sortablejs'
import { cloneDeep } from 'lodash'
import CreateLink from './components/createLink/CreateLink.vue'
import CreateLinks from './components/createLink/CreateLinks.vue'
import EditLink from './components/editLink/EditLink.vue'
import { ElMessage } from 'element-plus'
import defaultImg from '@/assets/png/短链默认图标.png'
import QRCode from './components/qrCode/QRCode.vue'

const backgroundImages = [
  '/story/11.webp',
  '/story/12.webp',
  '/story/13.webp',
  '/story/14.webp',
  '/story/15.webp',
  '/story/16.webp',
  '/story/17.webp',
  '/story/18.webp',
  '/story/19.webp'
]
const fallbackBackground = '/story/img676.webp'
const backgroundUrl = ref('')
const backgroundStyle = computed(() =>
  backgroundUrl.value ? { '--bg-image': `url(${backgroundUrl.value})` } : {}
)
const preloadedBackgrounds = new Set()

const preloadBackground = (src) =>
  new Promise((resolve, reject) => {
    if (!src || typeof window === 'undefined') {
      reject(new Error('invalid background'))
      return
    }
    if (preloadedBackgrounds.has(src)) {
      resolve()
      return
    }
    const img = new Image()
    img.onload = () => {
      preloadedBackgrounds.add(src)
      resolve()
    }
    img.onerror = () => {
      reject(new Error('background load failed'))
    }
    img.src = src
  })

const pickBackground = () =>
  backgroundImages[Math.floor(Math.random() * backgroundImages.length)] || fallbackBackground

const warmBackgroundCache = (current) => {
  if (typeof window === 'undefined') {
    return
  }
  const queue = backgroundImages.filter((item) => item && item !== current)
  let index = 0
  const step = () => {
    const src = queue[index]
    if (!src) {
      return
    }
    preloadBackground(src).catch(() => {})
    index += 1
    if (index < queue.length) {
      if ('requestIdleCallback' in window) {
        window.requestIdleCallback(step, { timeout: 1500 })
      } else {
        setTimeout(step, 200)
      }
    }
  }
  step()
}

const initBackground = () => {
  const candidate = pickBackground()
  backgroundUrl.value = candidate
  preloadBackground(candidate).catch(() => {
    backgroundUrl.value = fallbackBackground
  })
  warmBackgroundCache(candidate)
}

const orderIndex = ref(0)
const isMobile = ref(typeof window !== 'undefined' ? window.innerWidth <= 768 : false)
const updateIsMobile = () => {
  isMobile.value = window.innerWidth <= 768
}
const paginationLayout = computed(() =>
  isMobile.value ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'
)

const { proxy } = getCurrentInstance()
const API = proxy.$API
const router = useRouter()
const createLink1Ref = ref()
const createLink2Ref = ref()
let selectedIndex = ref(0)
const editableTabs = ref([])
const SELECTED_GID_KEY = 'short-link:myspace:selected-gid'
const SELECTED_VIEW_KEY = 'short-link:myspace:is-recycle'
// 添加弹窗关闭后重新请求一下页面数据
const afterAddLink = () => {
  setTimeout(() => {
    getGroupInfo(queryPage) // 重新请求数据
    // 按钮重新恢复可点击的样式
    document.querySelector('.addButton') && document.querySelector('.addButton').blur()
  }, 0)
  if (createLink1Ref.value) {
    createLink1Ref.value.initFormData()
  }
  if (createLink2Ref.value) {
    createLink2Ref.value.initFormData()
  }
  if (editLinkRef.value) {
    editLinkRef.value.initFormData()
  }
}
const resolveStatsQuery = (rowInfo) => {
  const info = rowInfo || {}
  const groupFlag = info.group ? '1' : '0'
  const groupCount =
    info.shortLinkCount ??
    editableTabs.value?.find((item) => item.gid === info.gid)?.shortLinkCount ??
    0
  return {
    fullShortUrl: info.fullShortUrl,
    gid: info.gid,
    group: groupFlag,
    originUrl: info.originUrl,
    favicon: info.favicon,
    enableStatus: info.enableStatus ?? '',
    describe: info.describe ?? info.name ?? '',
    nums: groupCount
  }
}
// ????????
const chartsVisible = (rowInfo) => {
  if (!rowInfo) {
    return
  }
  const query = resolveStatsQuery(rowInfo)
  router.push({ path: '/home/stats', query })
}
const transformGroupData = (oldIndex, newIndex) => {
  // 相当于直接对展示数据进行修改
  const formData = editableTabs.value
  const tempData = formData.splice(oldIndex, 1)
  // console.log('调整的值', tempData)
  formData.splice(newIndex, 0, tempData[0])
  // console.log('这是经过转化的1', formData)
  formData.forEach((item, index) => {
    item.sortOrder = index
  })
  // console.log('这是经过转化的2', formData)
  return formData
}
// 拖拽
const initSortable = (className) => {
  const table = document.querySelector('.' + className)
  if (!table) {
    return
  }
  // console.log(table)
  // 创建拖拽实例
  Sortable.create(table, {
    animation: 150, //动画
    handle: '.drag-handle',
    delay: isMobile.value ? 200 : 0,
    delayOnTouchOnly: true,
    touchStartThreshold: 8,
    // 开始拖动事件
    onStart: () => {
      // console.log('开始拖动')
    },
    // 结束拖动事件
    onEnd: async ({ to, from, oldIndex, newIndex, clone, pullMode }) => {
      // 当oldIndex不等于newIndex时才会去请求接口
      if (newIndex !== oldIndex) {
        // 对于不同情况下数据变化后的选中数据的实现
        if (selectedIndex.value === oldIndex) {
          selectedIndex.value = newIndex
        } else if (
          oldIndex < newIndex &&
          selectedIndex.value > oldIndex &&
          selectedIndex.value <= newIndex
        ) {
          selectedIndex.value = selectedIndex.value - 1
        } else if (
          oldIndex > newIndex &&
          selectedIndex.value < oldIndex &&
          selectedIndex.value >= newIndex
        ) {
          selectedIndex.value = selectedIndex.value + 1
        }
        const res = await API.group.sortGroup(transformGroupData(oldIndex, newIndex))
        // console.log('排序后的结果', res)
      }
    }
  })
}
// 改变选中分组时触发
watch(
  () => selectedIndex.value,
  (newValue) => {
    // -1为回收站，不需要重新请求正常页面数据
    if (newValue !== -1 && newValue !== -2) {
      queryPage()
    }
  }
)
onMounted(() => {
  initBackground()
  initSortable('sortOptions')
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateIsMobile)
})
const tableRef = ref()
const tableData = ref([])
const selectedRows = ref([])
const handleSelectionChange = (rows) => {
  selectedRows.value = rows ?? []
}
const getRowKey = (row) => row?.id ?? row?.fullShortUrl
const isRowSelected = (row) => {
  const key = getRowKey(row)
  return selectedRows.value.some((item) => getRowKey(item) === key)
}
const toggleRow = (row) => {
  const key = getRowKey(row)
  const index = selectedRows.value.findIndex((item) => getRowKey(item) === key)
  if (index === -1) {
    selectedRows.value = [...selectedRows.value, row]
  } else {
    const nextRows = selectedRows.value.slice()
    nextRows.splice(index, 1)
    selectedRows.value = nextRows
  }
}
const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  selectedRows.value = []
}
const pageParams = reactive({
  gid: null,
  current: 1,
  size: 15,
  orderTag: null
})
watch(
  () => pageParams.orderTag,
  (nV) => {
    queryPage()
  }
)
const totalNums = ref(0)
// 数据变化后更新当前页面
const queryPage = async () => {
  pageParams.gid = editableTabs.value?.[selectedIndex.value]?.gid
  const res = await API.smallLinkPage.queryPage(pageParams)
  if (res?.data.success) {
    tableData.value = res.data?.data?.records
    totalNums.value = +res.data?.data?.total
    nextTick(() => clearSelection())
  } else {
    ElMessage.error(res?.data.message)
  }
}

const handleSizeChange = () => {
  !isRecycleBin.value ? queryPage() : queryRecycleBinPage()
}

const handleCurrentChange = () => {
  !isRecycleBin.value ? queryPage() : queryRecycleBinPage()
}

// 是否展示回收站相关的组件
const isRecycleBin = ref(localStorage.getItem(SELECTED_VIEW_KEY) === '1')
if (isRecycleBin.value) {
  selectedIndex.value = -1
}
const recycleBinNums = ref(0) // 回收站中的数量

// 获取分组信息，更新页面的分组模块
const getGroupInfo = async (fn) => {
  const prevIndex = selectedIndex.value
  const res = await API.group.queryGroup()
  editableTabs.value = res.data?.data?.reverse()
  if (!isRecycleBin.value) {
    const storedGid = localStorage.getItem(SELECTED_GID_KEY)
    if (storedGid && editableTabs.value?.length) {
      const storedIndex = editableTabs.value.findIndex((item) => item.gid === storedGid)
      if (storedIndex >= 0) {
        selectedIndex.value = storedIndex
      }
    }
    if (editableTabs.value?.length && selectedIndex.value >= editableTabs.value.length) {
      selectedIndex.value = 0
    }
  }
  if (fn && selectedIndex.value === prevIndex) {
    fn()
  }
}
getGroupInfo(() => {
  if (isRecycleBin.value) {
    queryRecycleBinPage()
  } else {
    queryPage()
  }
})

const updatePage = () => {
  getGroupInfo(() => {
    if (isRecycleBin.value) {
      queryRecycleBinPage()
    } else {
      queryPage()
    }
  })
}

// 获取回收站页面，gid到时候要删除
const queryRecycleBinPage = () => {
  API.smallLinkPage
    .queryRecycleBin({ current: pageParams.current, size: pageParams.size })
    .then((res) => {
      tableData.value = res.data?.data?.records
      totalNums.value = +res.data?.data?.total
      recycleBinNums.value = totalNums.value
      nextTick(() => clearSelection())
    })
}
// 点击回收站
const recycleBin = () => {
  isRecycleBin.value = true
  selectedIndex.value = -1 // -1作为回收站，-2作为选中其他
  localStorage.setItem(SELECTED_VIEW_KEY, '1')
  clearSelection()
  pageParams.current = 1
  pageParams.size = 15
  // 点击回收站相关的请求
  queryRecycleBinPage()
}
// 点击分组中的选项
const changeSelectIndex = (index) => {
  selectedIndex.value = index
  isRecycleBin.value = false
  localStorage.setItem(SELECTED_VIEW_KEY, '0')
  clearSelection()
  const gid = editableTabs.value?.[index]?.gid
  if (gid) {
    localStorage.setItem(SELECTED_GID_KEY, gid)
  }
  // 对应分组的数据请求
}
// 添加分组相关
const isAddGroup = ref(false)
const newGroupName = ref()
const addGroupLoading = ref(false)
// 展示添加分组弹框
const showAddGroup = () => {
  newGroupName.value = ''
  isAddGroup.value = true
}
// 添加分组
const addGroup = async () => {
  addGroupLoading.value = true
  const res1 = await API.group.addGroup({ name: newGroupName.value })
  if (res1?.data.success) {
    ElMessage.success('添加成功')
    getGroupInfo(queryPage)
  } else {
    ElMessage.error(res1?.data.message)
  }
  isAddGroup.value = false
  addGroupLoading.value = false
}
// 删除分组
const deleteGroup = async (gid) => {
  const res = await API.group.deleteGroup({ gid })
  selectedIndex.value = 0
  if (res.data.success) {
    ElMessage.success('删除成功')
    getGroupInfo(queryPage)
  } else {
    ElMessage.error(res.data.message)
  }
}
// 编辑分组
const isEditGroup = ref(false)
const editGroupName = ref()
const editGid = ref('')
const editGroupLoading = ref(false)
// 点击编辑按钮，出现编辑弹框
const showEditGroup = (gid, name) => {
  editGid.value = gid
  editGroupName.value = name
  isEditGroup.value = true
}
// 编辑分组标题
const editGroup = async () => {
  editGroupLoading.value = true
  const res = await API.group.editGroup({ gid: editGid.value, name: editGroupName.value })
  if (res.data.success) {
    ElMessage.success('编辑成功')
    getGroupInfo(queryPage)
  } else {
    ElMessage.error('编辑失败')
  }
  isEditGroup.value = false
  editGroupLoading.value = false
}
// 创建短链
const isAddSmallLink = ref(false)
const isAddSmallLinks = ref(false)
// 关闭新建短链接弹窗
const addLink = () => {
  isAddSmallLink.value = false
  isAddSmallLinks.value = false
}
// 新建批量新建短链接弹窗
const cancelAddLink = () => {
  isAddSmallLink.value = false
  isAddSmallLinks.value = false
}
const getImgUrl = (url) => {
  return url ?? defaultImg
}
// 判断链接是否过期
const isExpire = (validDate) => {
  if (validDate) {
    const date = new Date(validDate).getTime()
    return new Date().getTime() < date
  }
}
// 复制链接
const copyUrl = (url) => {
  let eInput = document.createElement('input')
  eInput.value = url
  document.body.appendChild(eInput)
  eInput.select()
  let copyText = document.execCommand('Copy')
  eInput.style.display = 'none'
  if (copyText) {
    // console.log(eInput.value)
    ElMessage.success('链接复制成功!')
  }
}
// 修改短链信息
const isEditLink = ref(false) // 是否展示编辑弹框
const editLinkRef = ref()
const editData = ref() // 编辑弹框的数据
const editLink = (data) => {
  // console.log('data: ---' + data)
  editData.value = data
  isEditLink.value = true
}
// 隐藏弹框
const coverEditLink = () => {
  isEditLink.value = false
}
// 移动到回收站
const toRecycleBin = (data) => {
  const { gid, fullShortUrl } = data
  API.smallLinkPage
    .toRecycleBin({ gid, fullShortUrl })
    .then((res) => {
      if (res?.data?.code !== '0') {
        ElMessage.error(res.data.message)
      } else {
        ElMessage.success('删除成功')
        getGroupInfo(queryPage)
      }
    })
    .catch((reason) => {
      ElMessage.error('删除失败')
    })
}
const buildBatchPayload = () => {
  return (selectedRows.value || []).map((item) => ({
    gid: item.gid,
    fullShortUrl: item.fullShortUrl
  }))
}
const batchToRecycleBin = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要删除的短链接')
    return
  }
  API.smallLinkPage
    .toRecycleBinBatch(buildBatchPayload())
    .then((res) => {
      if (res?.data?.code !== '0') {
        ElMessage.error(res.data.message)
      } else {
        ElMessage.success('删除成功')
        clearSelection()
        getGroupInfo(queryPage)
      }
    })
    .catch(() => {
      ElMessage.error('删除失败')
    })
}
// 回收站中恢复
const recoverLink = (data) => {
  const { gid, fullShortUrl } = data
  API.smallLinkPage
    .recoverLink({ gid, fullShortUrl })
    .then((res) => {
      ElMessage.success('恢复成功')
      queryRecycleBinPage()
      // getGroupInfo(queryPage)
      getGroupInfo()         //修复短链接恢复会报系统执行出错的问题
    })
    .catch((reason) => {
      ElMessage.error('恢复失败')
    })
}
const batchRecoverLink = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要恢复的短链接')
    return
  }
  API.smallLinkPage
    .recoverLinkBatch(buildBatchPayload())
    .then((res) => {
      if (res?.data?.code !== '0') {
        ElMessage.error(res.data.message)
      } else {
        ElMessage.success('恢复成功')
        clearSelection()
        queryRecycleBinPage()
        getGroupInfo()
      }
    })
    .catch(() => {
      ElMessage.error('恢复失败')
    })
}
// 从回收站中删除
const removeLink = (data) => {
  const { gid, fullShortUrl } = data
  API.smallLinkPage
    .removeLink({ gid, fullShortUrl })
    .then((res) => {
      if (res?.data?.code !== '0') {
        ElMessage.error(res.data.message)
      } else {
        ElMessage.success('删除成功')
        queryRecycleBinPage()
      }
    })
    .catch((reason) => {
      ElMessage.error('删除失败')
    })
}
const batchRemoveLink = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要删除的短链接')
    return
  }
  API.smallLinkPage
    .removeLinkBatch(buildBatchPayload())
    .then((res) => {
      if (res?.data?.code !== '0') {
        ElMessage.error(res.data.message)
      } else {
        ElMessage.success('删除成功')
        clearSelection()
        queryRecycleBinPage()
      }
    })
    .catch(() => {
      ElMessage.error('删除失败')
    })
}
</script>

<style lang="scss" scoped>
.my-space-page {
  --ink: #0b1b2e;
  --ink-soft: #4d647c;
  --panel: rgba(255, 255, 255, 0.24);
  --panel-strong: rgba(255, 255, 255, 0.4);
  --accent: #00c9ff;
  --accent-strong: #007bff;
  --glow: rgba(0, 168, 255, 0.25);
  display: flex;
  height: 100%;
  position: relative;
  overflow: hidden;
  background-color: #eef4f9;
}

.my-space-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-color: #eef4f9;
  background-image: var(--bg-image, url('/story/img676.webp'));
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  filter: saturate(1.15) contrast(1.08) brightness(1.02);
  transform: scale(1.01);
  opacity: 1;
  z-index: 0;
  pointer-events: none;
}

.my-space-page::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(6, 12, 22, 0.04) 0%, rgba(6, 12, 22, 0) 16%);
  opacity: 1;
  z-index: 0;
  pointer-events: none;
}

.my-space-page > * {
  position: relative;
  z-index: 1;
}

.flex-box {
  display: flex;
  align-items: center;
  padding: 0 10px;
  justify-content: space-between;
}

.hover-box:hover {
  cursor: pointer;
  color: var(--accent);
  background-color: rgba(0, 200, 255, 0.12);
  box-shadow: 0px 10px 20px 0px rgba(0, 60, 120, 0.18);
}

.option-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  border-bottom: 1px solid rgba(0, 140, 210, 0.18);

  span {
    font-size: 12px;
    font-weight: 400;
    color: var(--ink-soft);
  }
}

.options-box {
  display: flex;
  flex-direction: column;
  position: relative;
  height: 100%;
  width: 190px;
  border-right: 1px solid rgba(0, 140, 210, 0.18);
  background: var(--panel);
  backdrop-filter: blur(4px) saturate(1.05);
  box-shadow: 0 10px 18px rgba(0, 40, 80, 0.06);

  .item-box {
    height: 43px;
    width: 190px;
    font-family:
      PingFangSC-Semibold,
      PingFang SC;
    font-weight: 520;
    color: var(--ink-soft);
  }

  .item-box:hover {
    .flex-box {
      .edit {
        display: block;
      }

      .item-length {
        display: none !important;
      }
    }
  }
}

.recycle-bin {
  position: absolute;
  display: flex;
  bottom: 0;
  height: 50px;
  width: 100%;
}

.recycle-box {
  flex: 1;
  border-top: 1px solid rgba(0, 140, 210, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-soft);
  background: rgba(255, 255, 255, 0.45);
}

.edit {
  display: none;
  margin-left: 5px;
  color: var(--ink-soft);
  font-size: 20px;
}

.edit:hover {
  color: var(--accent);
  cursor: pointer;
}

.drag-handle {
  cursor: grab;
}

.drag-handle:active {
  cursor: grabbing;
}

.zero {
  color: rgb(83, 97, 97) !important;
}

// 提示框样式
.tooltip-base-box {
  width: 600px;
}

.tooltip-base-box .row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tooltip-base-box .center {
  justify-content: center;
}

.tooltip-base-box .box-item {
  width: 110px;
}

.selectedItem {
  color: var(--accent) !important;
  background-color: rgba(0, 200, 255, 0.14) !important;
  font-weight: 600 !important;
}

.block:hover {
  color: var(--accent);

  .el-icon {
    color: var(--accent) !important;
  }
}

.table-edit {
  font-size: 20px;
  margin-right: 20px;
  color: var(--accent-strong);
  cursor: pointer;
}

.table-edit:hover {
  color: var(--accent);
}

.qr-code {
  margin-right: 20px;
  cursor: pointer;
}

.qr-code:hover {
  opacity: 0.5;
}

.content-box {
  flex: 1;
  padding: 16px;
  background-color: transparent;
  position: relative;

    .table-box {
      background-color: var(--panel-strong);
      height: 100%;
      border: 1px solid rgba(0, 140, 210, 0.2);
      box-shadow: 0 18px 32px rgba(0, 40, 90, 0.1), inset 0 0 0 1px rgba(0, 160, 255, 0.08);
      border-radius: 16px;
      overflow: hidden;
      backdrop-filter: blur(6px) saturate(1.02);

    .buttons-box {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px;
    }

    .pagination-block {
      position: absolute;
      bottom: 4%;
      left: 50%;
      transform: translate(-50%, 0);
    }

    .recycle-bin-box {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 16px;
      padding: 0 16px;

      .recycle-info {
        display: flex;
        align-items: center;

        span:nth-child(1) {
          font-size: 20px;
          margin-right: 5px;
          color: var(--ink);
        }

        span:nth-child(2) {
          color: var(--ink-soft);
        }
      }

      .recycle-actions {
        display: flex;
        align-items: center;
        gap: 10px;
      }
    }
  }
}

.over-text {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box; //作为弹性伸缩盒子模型显示。
  -webkit-box-orient: vertical; //设置伸缩盒子的子元素排列方式--从上到下垂直排列
  -webkit-line-clamp: 1; //显示的行
}

.table-link-box {
  display: flex;
  align-items: center;

  .name-date {
    display: flex;
    flex-direction: column;
    margin-left: 10px;

    span:nth-child(1) {
      font-size: 15px;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box; //作为弹性伸缩盒子模型显示。
      -webkit-box-orient: vertical; //设置伸缩盒子的子元素排列方式--从上到下垂直排列
      -webkit-line-clamp: 1; //显示的行
    }

    .time {
      display: flex;
      align-items: center;

      span {
        font-size: 12px;
      }

      img {
        margin-left: 10px;
      }

      div {
        border: 1.5px solid rgb(253, 81, 85);
        border-radius: 8px;
        line-height: 20px;
        font-size: 12px;
        transform: scale(0.7);
        color: rgb(253, 81, 85);
        padding: 0 4px;
        background-color: rgba(250, 210, 211);

        span {
          font-weight: bolder;
        }
      }
    }
  }
}

.isExpire {
  .name-date {
    span:nth-child(1) {
      color: rgba(0, 0, 0, 0.3);
    }

    .time {
      div {
        span {
          font-weight: bolder;
          color: red;
        }
      }
    }
  }
}

.table-url-box {
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box; //作为弹性伸缩盒子模型显示。
    -webkit-box-orient: vertical; //设置伸缩盒子的子元素排列方式--从上到下垂直排列
    -webkit-line-clamp: 1; //显示的行
    color: rgba(16, 42, 66, 0.72);
  }
}

.times-box {
  display: flex;
  flex-direction: column;

  .today-box {
    span {
      font-size: 13px;
      font-weight: 600;
      margin-right: 10px;
      color: var(--ink);
    }

    span:nth-child(1) {
      font-weight: 400;
      color: var(--ink-soft);
    }
  }

  .total-box {
    span {
      font-size: 13px;
      font-weight: 400;
      margin-right: 10px;
      color: var(--ink);
    }

    span:nth-child(1) {
      font-weight: 400;
      color: var(--ink-soft);
    }
  }
}

.copy-url {
  margin-left: 10px;
}

.demo-tabs>.el-tabs__content {
  font-size: 32px;
  font-weight: 600;
}

.demo-tabs .custom-tabs-label .el-icon {
  vertical-align: middle;
}

.demo-tabs .custom-tabs-label span {
  vertical-align: middle;
  margin-left: 4px;
}

.orderIndex {
  color: var(--accent);
}

.sortOptions {
  height: calc(100% - 50px);
  margin-bottom: 50px;
  // height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  list-style: none;
  padding-left: 0;
  margin-top: 0;
}

.addButton {
  box-shadow: 0 10px 18px rgba(33, 200, 255, 0.35);
}

:deep(.el-table) {
  background: transparent;
  color: var(--ink);
}

:deep(.el-table__inner-wrapper::before) {
  background-color: transparent;
}

:deep(.el-table tr) {
  background-color: transparent;
}

:deep(.el-table__header th) {
  background: rgba(255, 255, 255, 0.6) !important;
  border-bottom: 1px solid rgba(0, 140, 210, 0.2) !important;
  color: var(--ink);
}

:deep(.el-table__cell) {
  border-bottom: 1px solid rgba(0, 140, 210, 0.12);
}

:deep(.el-table__body tr:hover > td) {
  background-color: rgba(0, 200, 255, 0.12) !important;
}

:deep(.el-checkbox__inner) {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: rgba(0, 140, 210, 0.35);
}

:deep(.el-link) {
  color: var(--accent);
}

:deep(.el-link:hover) {
  color: #8ef9ff;
}

:deep(.el-pagination) {
  --el-color-primary: var(--accent);
  color: var(--ink-soft);
}

:deep(.el-button) {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 140, 210, 0.22);
  color: var(--ink);
  box-shadow: 0 8px 14px rgba(0, 70, 120, 0.12);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--accent), var(--accent-strong));
  border: none;
  color: #06101a;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ff5f6d, #ff8b5f);
  border: none;
  color: #16090c;
}

@media (max-width: 768px) {
  .my-space-page {
    flex-direction: column;
    height: auto;
    overflow: auto;
  }

  .options-box {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid rgba(0, 140, 210, 0.18);
    padding-bottom: 8px;
  }

  .options-box .item-box .edit,
  .options-box .item-box .el-dropdown {
    display: none !important;
  }

  .option-title {
    padding: 0 12px;
    height: 52px;
  }

  .option-title .hover-box {
    width: 36px !important;
    height: 36px;
    border-radius: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 200, 255, 0.12);
  }

  .option-title .hover-box img {
    width: 18px;
    height: 18px;
  }

  .options-box .item-box {
    width: auto;
  }

  .sortOptions {
    height: auto;
    margin-bottom: 0;
    overflow-x: hidden;
    overflow-y: visible;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 8px 12px 12px;
  }

  .sortOptions li {
    flex: 1 1 calc(50% - 8px);
  }

  .sortOptions .item-box {
    min-height: 52px;
    height: auto;
    width: 100%;
    padding: 8px 12px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.5);
    border: 1px solid rgba(0, 140, 210, 0.22);
    box-shadow: 0 8px 14px rgba(0, 40, 80, 0.12);
  }

  .sortOptions .item-box.flex-box {
    padding: 0;
    gap: 8px;
    width: 100%;
    justify-content: space-between;
  }

  .sortOptions .item-box > div:first-child {
    flex: 1 1 auto;
    min-width: 0;
    align-items: center;
    padding-right: 8px;
  }

  .sortOptions .item-box > .flex-box {
    width: auto;
    flex: 0 0 auto;
    gap: 6px;
    padding: 0;
  }

  .sortOptions .item-box img {
    display: none;
  }

  .sortOptions .item-box .drag-handle {
    display: inline-flex;
    width: 18px;
    height: 18px;
    margin-right: 6px;
    opacity: 0.6;
  }

  .sortOptions .item-box .over-text {
    max-width: 100%;
    font-size: 13px;
    display: block;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: var(--ink);
  }

  .sortOptions .item-box .item-length {
    display: inline-flex !important;
    align-items: center;
    justify-content: center;
    height: 20px;
    min-width: 20px;
    padding: 0 6px;
    border-radius: 999px;
    background: rgba(0, 200, 255, 0.15);
    color: var(--ink);
    font-size: 12px;
  }

  .recycle-bin {
    position: relative;
    height: auto;
    padding: 0 12px 8px;
  }

  .recycle-box {
    justify-content: center;
    gap: 6px;
    padding: 10px 12px;
    height: 46px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.45);
    border: 1px solid rgba(0, 140, 210, 0.22);
    box-shadow: 0 8px 14px rgba(0, 40, 80, 0.12);
    font-size: 14px;
    margin-top: 4px;
  }

  .recycle-box .el-icon {
    font-size: 18px;
  }

  .content-box {
    width: 100%;
    flex: none;
  }

  .content-box .table-box {
    height: auto;
    overflow: visible;
  }

  .buttons-box > div {
    flex-wrap: wrap;
    gap: 8px;
  }

  .recycle-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .recycle-bin-box {
    height: auto !important;
    padding: 12px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .recycle-bin-box .recycle-info span:nth-child(1) {
    font-size: 18px;
  }

  .recycle-bin-box .recycle-actions {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 8px;
  }

  .recycle-bin-box .recycle-actions :deep(.el-button) {
    width: 100%;
    height: 44px;
    border-radius: 12px;
  }

  :deep(.myspace-table) {
    width: 100% !important;
  }

  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }

  .table-link-box {
    align-items: flex-start;
  }

  .mobile-url {
    margin-top: 6px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 12px;
    color: var(--ink-soft);
  }

  .mobile-url .origin-url {
    color: var(--ink-soft);
    word-break: break-all;
  }

  .mobile-stats {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px 10px;
  }

  .mobile-stats .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 2px 8px;
    border-radius: 999px;
    background: rgba(0, 200, 255, 0.12);
    border: 1px solid rgba(0, 200, 255, 0.22);
    font-size: 12px;
    color: var(--ink);
  }

  .mobile-stats .label {
    opacity: 0.7;
  }

  .card-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 8px 0 4px;
  }

  .card-empty {
    padding: 40px 0;
    text-align: center;
    color: var(--ink-soft);
  }

  .link-card {
    background: var(--panel-strong);
    border: 1px solid rgba(0, 140, 210, 0.2);
    border-radius: 18px;
    padding: 12px;
    box-shadow: 0 12px 22px rgba(0, 40, 80, 0.12);
  }

  .link-card.expired {
    border-color: rgba(253, 81, 85, 0.35);
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    gap: 8px;
  }

  .card-select {
    margin-top: 2px;
  }

  :deep(.card-select .el-checkbox__inner) {
    width: 18px;
    height: 18px;
  }

  :deep(.card-select .el-checkbox__inner::after) {
    width: 6px;
    height: 10px;
    left: 5px;
    top: 1px;
  }

  .card-title {
    flex: 1;
    min-width: 0;
  }

  .card-title .title {
    font-size: 15px;
    font-weight: 600;
    color: var(--ink);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .card-title .time {
    margin-top: 4px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
    font-size: 12px;
    color: var(--ink-soft);
  }

  .expire-tag {
    border: 1.5px solid rgb(253, 81, 85);
    border-radius: 8px;
    line-height: 18px;
    font-size: 11px;
    transform: scale(0.9);
    color: rgb(253, 81, 85);
    padding: 0 6px;
    background-color: rgba(250, 210, 211, 0.8);
  }

  .card-url {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .card-url .origin {
    font-size: 12px;
    color: var(--ink-soft);
    word-break: break-all;
  }

  .card-stats {
    margin-top: 12px;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .stat-card {
    padding: 8px 6px;
    border-radius: 14px;
    text-align: center;
    background: rgba(0, 200, 255, 0.12);
    border: 1px solid rgba(0, 200, 255, 0.22);
  }

  .stat-card .stat-label {
    font-size: 12px;
    color: var(--ink-soft);
  }

  .stat-card .stat-values {
    margin-top: 4px;
    display: flex;
    flex-direction: column;
    gap: 2px;
    font-size: 13px;
    color: var(--ink);
  }

  .card-tools {
    margin-top: 12px;
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;
  }

  .tool-btn {
    flex: 1;
    height: 44px;
    border-radius: 12px;
  }

  .qr-tool {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 0 12px;
    height: 44px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.75);
    border: 1px solid rgba(0, 140, 210, 0.22);
    color: var(--ink);
  }

  .card-actions {
    margin-top: 12px;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(110px, 1fr));
    gap: 8px;
  }

  .action-btn {
    height: 46px;
    border-radius: 14px;
    font-size: 14px;
  }

  :deep(.qr-tool img) {
    width: 26px;
    height: 26px;
  }

  .content-box .table-box .pagination-block {
    margin-top: 14px;
    padding: 8px 0 20px;
    display: flex;
    justify-content: center;
    position: static;
    transform: none;
    left: auto;
    bottom: auto;
    width: 100%;
  }

  :deep(.el-pagination) {
    gap: 4px;
    width: 100%;
    justify-content: center;
  }

  .table-box.is-recycle .buttons-box {
    display: none !important;
  }

  :deep(.el-pagination button),
  :deep(.el-pagination .el-pager li) {
    min-width: 28px;
    height: 28px;
    line-height: 28px;
    font-size: 12px;
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
</style>
