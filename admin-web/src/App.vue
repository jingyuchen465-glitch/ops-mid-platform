<script setup>
import { computed, h, onMounted, onUnmounted, ref } from 'vue'
import { ADMIN_TOKEN_KEY, UNAUTHORIZED_EVENT, api } from './api'
import LoginView from './LoginView.vue'
import { AppstoreOutlined, AuditOutlined, KeyOutlined, RocketOutlined, SettingOutlined, TeamOutlined, ThunderboltOutlined, PlusOutlined, SearchOutlined, BellOutlined, UserOutlined, DatabaseOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'

const page = ref('dashboard')
const loggedIn = ref(Boolean(localStorage.getItem(ADMIN_TOKEN_KEY)))
const loading = ref(false)
const rows = ref([])
const dashboard = ref({})
const roles = ref([])
const roleTools = ref([])
const tokenSelections = ref([])
const dynamicToolOptions = ref([])
const toolPermissionOpen = ref(false)
const tokenSelectionOpen = ref(false)
const activeRole = ref(null)
const activeToken = ref(null)
const selectedRoleTools = ref([])
const selectedTokenTools = ref([])
const users = ref([])
const drawer = ref(false)
const model = ref({})
const rawToken = ref('')
const builtinTools = [
  { name: 'hello', description: '问候工具' },
  { name: 'current_time', description: '当前时间' },
  { name: 'system_info', description: '系统信息' },
  { name: 'calculate', description: '数学计算' }
]
const tokenPermissionOptions = [
  { label: '工具列表 + 工具调用', value: '["mcp:tools:read","mcp:tools:call"]' },
  { label: '仅查看工具列表', value: '["mcp:tools:read"]' }
]
const activeStatusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]
const menu = [
  ['dashboard', '概览', AppstoreOutlined], ['users', '用户', TeamOutlined], ['roles', '角色与工具权限', TeamOutlined], ['tokens', 'Token 与工具选择', KeyOutlined],
  ['requests', '请求配置', SettingOutlined], ['tools', '动态工具', ThunderboltOutlined], ['audits', '审计日志', AuditOutlined]
]
const navSections = { dashboard: '概览', users: '系统治理', roles: '系统治理', tokens: '访问控制', requests: '能力展示', tools: '能力展示', audits: '运行观测' }
const title = computed(() => ({ dashboard:'运行概览', users:'用户', roles:'角色与工具权限', tokens:'Token 与工具选择', requests:'请求配置', tools:'动态工具', audits:'调用审计' })[page.value])
const desc = computed(() => ({ dashboard:'当前数据库中的 MCP 治理状态', users:'角色是工具权限上限，用户通过角色获得资格', roles:'角色决定资格上限，实际工具权限由角色工具表维护', tokens:'每把 Token 单独选择要暴露和实际允许调用的工具', requests:'展示动态工具可引用的企业请求配置；完整创作在创作空间完成', tools:'这里只展示已发布的动态工具；创建与编辑在创作空间完成', audits:'保留每一次 MCP 工具调用的结果摘要与耗时' })[page.value])
const userOptions = computed(() => users.value.map(user => ({
  label: `${user.displayName || user.username}（${user.username}）`,
  value: user.id
})))
const columns = computed(() => ({
 users:[['username','用户名'],['displayName','展示名称'],['isEnabled','状态']],
 tokens:[['tokenName','Token 名称'],['userId','所属用户'],['tokenPrefix','展示前缀'],['isActive','状态'],['expireTime','过期时间']],
 requests:[['requestId','接口 ID'],['configKey','配置 Key'],['name','名称'],['type','协议'],['publishStatus','发布'],['isEnabled','状态']],
 tools:[['toolName','工具名'],['toolDescription','工具描述'],['enabled','状态']],
 audits:[['createTime','调用时间'],['userName','用户'],['toolName','工具'],['status','状态'],['durationMs','耗时(ms)']],
 roles:[['roleCode','角色编码'],['roleName','角色名称'],['isEnabled','状态']]
})[page.value] || [])
const dataColumns = computed(() => columns.value.map(([dataIndex,title]) => ({
  title,
  dataIndex,
  ellipsis:true,
  customRender: ({ text }) => formatTableCell(dataIndex, text)
})))

function formatTableCell(dataIndex, text) {
  if (dataIndex === 'userId' && page.value === 'tokens') {
    return userLabel(text)
  }
  if (dataIndex === 'isEnabled' || dataIndex === 'isActive' || dataIndex === 'enabled') {
    return Number(text) === 1 ? '启用' : '禁用'
  }
  if (dataIndex === 'publishStatus') {
    return ['草稿','上架不公开','公开'][Number(text)] || '-'
  }
  return text ?? '-'
}

function userLabel(userId) {
  const user = users.value.find(item => item.id === userId)
  if (!user) {
    return userId ?? '-'
  }
  return `${user.displayName || user.username}（${user.username}）`
}

async function load() {
  loading.value = true
  try {
    if (page.value === 'dashboard') { dashboard.value = await api('/dashboard'); rows.value = dashboard.value.recentAudits || [] }
    else if (page.value === 'users') { [rows.value, roles.value] = await Promise.all([api('/users'), api('/roles')]); users.value = rows.value }
    else if (page.value === 'roles') { [rows.value, roleTools.value, dynamicToolOptions.value] = await Promise.all([api('/roles'), api('/role-tools'), api('/dynamic-tools')]) }
    else if (page.value === 'tokens') {
      [rows.value, users.value, tokenSelections.value, dynamicToolOptions.value] = await Promise.all([
        api('/tokens'),
        api('/users'),
        api('/token-selections'),
        api('/dynamic-tools')
      ])
    }
    else if (page.value === 'requests') rows.value = await api('/request-configs')
    else if (page.value === 'tools') rows.value = await api('/dynamic-tools')
    else rows.value = await api('/audit-logs')
  } catch (error) {
    if (loggedIn.value) {
      console.error(error)
    }
  } finally { loading.value = false }
}
function changePage(key) { page.value = key; rawToken.value=''; load() }
function emptyModel() {
  if (page.value === 'users') return { username:'', displayName:'', password:'', isEnabled:1 }
  if (page.value === 'roles') return { roleCode:'', roleName:'', description:'', isEnabled:1 }
  if (page.value === 'tokens') return { userId: users.value[0]?.id, tokenName:'新建 Token', permissions:'["mcp:tools:read","mcp:tools:call"]', isActive:1 }
  if (page.value === 'requests') return { requestId:'API' + Date.now(), configKey:'', name:'', type:'HTTP', method:'GET', headers:'{}', paramsDefault:'{}', connectTimeoutMs:5000, readTimeoutMs:15000, isEnabled:1, rateLimitPerMinute:0, publishStatus:0 }
  if (page.value === 'tools') return { toolName:'', toolDescription:'', inputSchema:'{"type":"object","properties":{}}', groovyScript:'return [message: params.message]', linkedRequestKeys:'[]', enabled:1 }
  return {}
}
function openCreate() { model.value = emptyModel(); drawer.value=true }
function edit(row) { model.value = { ...row }; drawer.value=true }
async function save() {
  const base = { users:'/users', roles:'/roles', tokens:'/tokens', requests:'/request-configs', tools:'/dynamic-tools' }[page.value]
  const method = model.value.id ? 'PUT' : 'POST'
  const body = buildSaveBody()
  const result = await api(model.value.id ? `${base}/${model.value.id}` : base, { method, body })
  if (page.value === 'tokens' && result?.rawToken) rawToken.value = result.rawToken
  drawer.value = false; await load()
}
function buildSaveBody() {
  const body = { ...model.value }
  if (page.value === 'tokens' && !body.expireTime) {
    delete body.expireTime
  }
  return body
}
async function updateSelections(token) {
  activeToken.value = token
  selectedTokenTools.value = tokenSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.toolName)
  tokenSelectionOpen.value = true
}
async function updateUserRoles(user) {
  const names = prompt('输入该用户的角色编码，逗号分隔，例如 ADMIN,DEVELOPER')
  if (names === null) return
  await api(`/users/${user.id}/roles`, { method:'PUT', body:{ codes:names.split(',').map(x=>x.trim()).filter(Boolean) } }); alert('用户角色已保存')
}
async function updateRoleTools(role) {
  activeRole.value = role
  selectedRoleTools.value = roleTools.value.filter(item => item.roleCode === role.roleCode).map(item => item.toolName)
  toolPermissionOpen.value = true
}
async function saveRoleTools() {
  await api(`/roles/${activeRole.value.roleCode}/tools`, { method:'PUT', body:{ codes:selectedRoleTools.value } })
  toolPermissionOpen.value = false
  await load()
}
async function saveTokenSelections() {
  const builtinToolNames = builtinTools.map(tool => tool.name)
  const tools = selectedTokenTools.value.map(toolName => ({
    toolName,
    toolType: builtinToolNames.includes(toolName) ? 'BUILTIN' : 'DYNAMIC',
    enabled: 1
  }))

  await api(`/tokens/${activeToken.value.id}/selections`, {
    method: 'PUT',
    body: { tools }
  })

  tokenSelectionOpen.value = false
  await load()
}
function showLogin() {
  loggedIn.value = false
  rows.value = []
  drawer.value = false
  toolPermissionOpen.value = false
  tokenSelectionOpen.value = false
  rawToken.value = ''
}

onMounted(() => {
  window.addEventListener(UNAUTHORIZED_EVENT, showLogin)

  if (loggedIn.value) {
    load()
  }
})

onUnmounted(() => {
  window.removeEventListener(UNAUTHORIZED_EVENT, showLogin)
})

function loginSuccess() {
  loggedIn.value = true
  load()
}
</script>

<template>
  <LoginView v-if="!loggedIn" @success="loginSuccess" />
  <template v-else>
  <a-config-provider :theme="{ token: { colorPrimary: '#8b5cf6', borderRadius: 8, fontFamily: 'Inter, PingFang SC, Microsoft YaHei, sans-serif' } }">
  <a-layout class="console-layout">
    <a-layout-sider width="260" class="console-sider">
      <div class="brand"><span class="brand-mark">B</span><span>Bear MCP<small>管理控制台</small></span></div>
      <a-menu theme="dark" mode="inline" :selected-keys="[page]" class="console-menu" @click="({key}) => changePage(key)">
        <div class="nav-caption">概览</div>
        <a-menu-item key="dashboard"><AppstoreOutlined /><span>概览</span></a-menu-item>
        <div class="nav-caption">系统治理</div>
        <a-menu-item key="users"><TeamOutlined /><span>用户</span></a-menu-item>
        <a-menu-item key="roles"><SafetyCertificateOutlined /><span>角色与工具权限</span></a-menu-item>
        <div class="nav-caption">访问控制</div>
        <a-menu-item key="tokens"><KeyOutlined /><span>Token 与工具选择</span></a-menu-item>
        <div class="nav-caption">能力展示</div>
        <a-menu-item key="requests"><SettingOutlined /><span>请求配置</span></a-menu-item>
        <a-menu-item key="tools"><ThunderboltOutlined /><span>动态工具</span></a-menu-item>
        <div class="nav-caption">运行观测</div>
        <a-menu-item key="audits"><AuditOutlined /><span>审计日志</span></a-menu-item>
      </a-menu>
      <div class="sider-footer"><span class="live-dot"></span><span>MCP Server Online</span><small>v0.1.0 · Lesson 6</small></div>
    </a-layout-sider>
    <a-layout-content class="layout-content">
      <header class="console-topbar"><div class="top-search"><SearchOutlined /><span>搜索页面、工具或配置</span><kbd>⌘ K</kbd></div><div class="top-actions"><a-button type="text" shape="circle" :icon="h(BellOutlined)" /><div class="user-chip"><span class="avatar">D</span><span><b>demo-admin</b><small>管理员</small></span></div></div></header>
      <div class="page-head"><div><div class="breadcrumb">MCP 管理后台 <span>/</span> {{ navSections[page] }}</div><h1 class="page-title">{{ title }}</h1><div class="page-desc">{{ desc }}</div></div><a-button v-if="['users','roles','tokens','requests'].includes(page)" type="primary" class="create-btn" :icon="h(PlusOutlined)" @click="openCreate">新建{{ title.replace('与工具权限','').replace('与工具选择','') }}</a-button></div>
      <template v-if="page === 'dashboard'">
        <a-row :gutter="16" class="metric-grid"><a-col v-for="[label,key,icon,color] in [['用户', 'users', TeamOutlined, 'violet'],['角色','roles',SafetyCertificateOutlined, 'cyan'],['有效 Token','activeTokens',KeyOutlined, 'orange'],['启用请求','enabledRequests',DatabaseOutlined, 'green'],['动态工具','enabledDynamicTools',ThunderboltOutlined, 'pink']]" :key="key" :span="4"><a-card class="metric"><div class="metric-top"><span>{{ label }}</span><span :class="['metric-icon', color]"><component :is="icon" /></span></div><a-statistic :value="dashboard[key] || 0" /><div class="metric-note"><span class="trend">●</span> 当前数据库统计</div></a-card></a-col></a-row>
        <div class="surface dashboard-table"><div class="table-toolbar"><div><b>最近调用</b><small>最新 100 条 MCP 工具调用记录</small></div><a-button @click="load">刷新数据</a-button></div><a-table :data-source="rows" :columns="[{title:'时间',dataIndex:'createTime'},{title:'工具',dataIndex:'toolName'},{title:'用户',dataIndex:'userName'},{title:'状态',dataIndex:'status'},{title:'耗时(ms)',dataIndex:'durationMs'}]" row-key="id" :pagination="false" /></div>
      </template>
      <template v-else><div class="surface"><div class="table-toolbar"><div><b>{{ title }}列表</b><small>共 {{ rows.length }} 条记录<span v-if="page==='tools'"> · 由创作空间发布</span></small></div><div class="table-tools"><a-input placeholder="搜索名称或编码" class="table-search"><template #prefix><SearchOutlined /></template></a-input><a-button @click="load">刷新</a-button></div></div><a-table :loading="loading" :data-source="rows" :columns="[...dataColumns,...(['audits','tools'].includes(page)?[]:[{title:'操作',key:'action'}])]" row-key="id"><template #bodyCell="{column,record}"><template v-if="column.key==='action'"><a-button v-if="page==='users'" type="link" @click="updateUserRoles(record)">分配角色</a-button><a-button v-if="page==='roles'" type="link" @click="updateRoleTools(record)">配置工具</a-button><a-button v-if="page==='tokens'" type="link" @click="updateSelections(record)">工具选择</a-button><a-button type="link" @click="edit(record)">编辑</a-button></template></template></a-table></div></template>
    </a-layout-content>
  </a-layout>
  <a-drawer v-model:open="drawer" :title="`编辑${title}`" width="560" class="console-drawer" @close="rawToken=''">
    <a-form v-if="page === 'tokens'" layout="vertical" class="token-form">
      <section class="form-section">
        <div class="form-section-head">
          <b>归属与名称</b>
          <span>这把 Token 属于哪个管理用户</span>
        </div>

        <a-form-item label="所属用户">
          <a-select
            v-model:value="model.userId"
            :options="userOptions"
            placeholder="请选择用户"
            size="large"
            show-search
            option-filter-prop="label"
            popupClassName="dark-select-dropdown"
          />
        </a-form-item>

        <a-form-item label="Token 名称">
          <a-input v-model:value="model.tokenName" size="large" placeholder="例如：课堂演示 Token" />
        </a-form-item>
      </section>

      <section class="form-section">
        <div class="form-section-head">
          <b>访问范围</b>
          <span>控制这把 Token 可以读取工具列表还是可以调用工具</span>
        </div>

        <a-radio-group v-model:value="model.permissions" class="permission-card-group">
          <a-radio-button
            v-for="option in tokenPermissionOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-radio-button>
        </a-radio-group>
      </section>

      <section class="form-section compact">
        <a-form-item label="状态">
          <a-segmented
            v-model:value="model.isActive"
            :options="activeStatusOptions"
            block
            class="status-segmented"
          />
        </a-form-item>

        <a-form-item label="过期时间">
          <a-input v-model:value="model.expireTime" size="large" placeholder="不填表示不过期" />
        </a-form-item>
      </section>
    </a-form>
    <a-form v-else layout="vertical">
      <template v-for="(value,key) in model" :key="key"><a-form-item v-if="!['id','tokenHash','tokenPrefix','createTime','updateTime','lastUsedTime','lastUsedIp'].includes(key)" :label="key"><a-textarea v-if="['headers','bodyTemplate','paramsDefault','inputSchema','groovyScript','linkedRequestKeys','description','argsSchema'].includes(key)" v-model:value="model[key]" class="code-area" :auto-size="{minRows:2,maxRows:8}" /><a-input v-else v-model:value="model[key]" /></a-form-item></template>
    </a-form>
    <div v-if="rawToken" class="raw-token"><b>请立即保存 Token，之后后台不会再返回完整明文：</b><br>{{ rawToken }}</div>
    <template #footer><a-space><a-button @click="drawer=false">取消</a-button><a-button type="primary" @click="save">保存</a-button></a-space></template>
  </a-drawer>
  <a-modal v-model:open="toolPermissionOpen" :title="`配置工具权限 · ${activeRole?.roleName || ''}`" width="720px" class="tool-permission-modal" @ok="saveRoleTools">
    <p class="permission-hint">角色工具权限是资格上限。Token 是否实际展示和调用工具，还需要在 Token 工具选择中单独配置。</p>
    <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot builtin"></span>内置工具 <small>由 Spring AI 注册</small></div><a-checkbox-group v-model:value="selectedRoleTools" class="tool-option-grid"><a-checkbox v-for="tool in builtinTools" :key="tool.name" :value="tool.name"><b>{{ tool.name }}</b><span>{{ tool.description }}</span></a-checkbox></a-checkbox-group></section>
    <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot dynamic"></span>动态工具 <small>由创作空间发布</small></div><a-checkbox-group v-model:value="selectedRoleTools" class="tool-option-grid"><a-checkbox v-for="tool in dynamicToolOptions" :key="tool.toolName" :value="tool.toolName" :disabled="tool.enabled !== 1"><b>{{ tool.toolName }}</b><span>{{ tool.toolDescription || '暂无描述' }}</span></a-checkbox></a-checkbox-group><a-empty v-if="!dynamicToolOptions.length" description="暂无已发布动态工具" :image-style="{height:'48px'}" /></section>
    <template #footer><a-button @click="toolPermissionOpen=false">取消</a-button><a-button type="primary" @click="saveRoleTools">保存工具权限</a-button></template>
  </a-modal>
  <a-modal v-model:open="tokenSelectionOpen" :title="`Token 工具选择 · ${activeToken?.tokenName || ''}`" width="720px" class="tool-permission-modal" @ok="saveTokenSelections">
    <p class="permission-hint">Token 工具选择决定这把 Token 实际加载、展示和允许调用哪些工具；最终调用还会再经过角色工具权限校验。</p>
    <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot builtin"></span>内置工具 <small>由 Spring AI 注册</small></div><a-checkbox-group v-model:value="selectedTokenTools" class="tool-option-grid"><a-checkbox v-for="tool in builtinTools" :key="tool.name" :value="tool.name"><b>{{ tool.name }}</b><span>{{ tool.description }}</span></a-checkbox></a-checkbox-group></section>
    <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot dynamic"></span>动态工具 <small>由创作空间发布</small></div><a-checkbox-group v-model:value="selectedTokenTools" class="tool-option-grid"><a-checkbox v-for="tool in dynamicToolOptions" :key="tool.toolName" :value="tool.toolName" :disabled="tool.enabled !== 1"><b>{{ tool.toolName }}</b><span>{{ tool.toolDescription || '暂无描述' }}</span></a-checkbox></a-checkbox-group><a-empty v-if="!dynamicToolOptions.length" description="暂无已发布动态工具" :image-style="{height:'48px'}" /></section>
    <template #footer><a-button @click="tokenSelectionOpen=false">取消</a-button><a-button type="primary" @click="saveTokenSelections">保存工具选择</a-button></template>
  </a-modal>
  </a-config-provider>
  </template>
</template>
