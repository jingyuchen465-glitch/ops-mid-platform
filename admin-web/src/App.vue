<script setup>
import { computed, h, onMounted, onUnmounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { ADMIN_TOKEN_KEY, UNAUTHORIZED_EVENT, api } from './api'
import LoginView from './LoginView.vue'
import { AppstoreOutlined, AuditOutlined, KeyOutlined, RocketOutlined, SettingOutlined, TeamOutlined, ThunderboltOutlined, PlusOutlined, SearchOutlined, BellOutlined, DatabaseOutlined, SafetyCertificateOutlined, HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'

const page = ref(pageFromPath())
const loggedIn = ref(Boolean(localStorage.getItem(ADMIN_TOKEN_KEY)))
const loading = ref(false)
const rows = ref([])
const dashboard = ref({})
const roles = ref([])
const roleTools = ref([])
const rolePrompts = ref([])
const roleResources = ref([])
const userRoles = ref([])
const tokenSelections = ref([])
const tokenPromptSelections = ref([])
const tokenResourceSelections = ref([])
const dynamicToolOptions = ref([])
const promptOptions = ref([])
const resourceOptions = ref([])
const toolPermissionOpen = ref(false)
const promptPermissionOpen = ref(false)
const resourcePermissionOpen = ref(false)
const tokenSelectionOpen = ref(false)
const tokenPromptSelectionOpen = ref(false)
const tokenResourceSelectionOpen = ref(false)
const userRoleOpen = ref(false)
const activeUser = ref(null)
const activeRole = ref(null)
const activeToken = ref(null)
const selectedUserRoles = ref([])
const selectedRoleTools = ref([])
const selectedRolePrompts = ref([])
const selectedRoleResources = ref([])
const selectedTokenTools = ref([])
const selectedTokenPrompts = ref([])
const selectedTokenResources = ref([])
const users = ref([])
const requestConfigs = ref([])
const dataSourceConfigs = ref([])
const drawer = ref(false)
const model = ref({})
const rawToken = ref('')
const oneTimeTokenOpen = ref(false)
const oneTimeTokenName = ref('')
const dynamicToolDetailOpen = ref(false)
const activeDynamicTool = ref(null)
const communityDetailOpen = ref(false)
const activeCommunityItem = ref(null)
const auditDetailOpen = ref(false)
const activeAuditLog = ref(null)
const apiDebugOpen = ref(false)
const activeStudioApi = ref(null)
const debugParams = ref('{}')
const debugResult = ref(null)
const debugLoading = ref(false)
const apiSaving = ref(false)
const dataSourceTesting = ref(false)
const apiEditorTab = ref('body')
const apiEditorDebugParams = ref('{}')
const apiKeyword = ref('')
const apiStatusFilter = ref('all')
const skillKeyword = ref('')
const skillStatusFilter = ref('all')
const skillPreviewText = ref('')
const skillPreviewLoading = ref(false)
const skillPreviewHtml = computed(() => renderMarkdown(skillPreviewText.value))
const skillSaving = ref(false)
const skillSelectedFile = ref(null)
const toolKeyword = ref('')
const toolStatusFilter = ref('all')
const promptKeyword = ref('')
const promptStatusFilter = ref('all')
const resourceKeyword = ref('')
const resourceStatusFilter = ref('all')
const resourcePreviewText = ref('')
const resourcePreviewLoading = ref(false)
const resourcePreviewHtml = computed(() => renderMarkdown(resourcePreviewText.value))
const promptPreviewArgs = ref('{}')
const promptPreviewResult = ref(null)
const promptPreviewLoading = ref(false)
const promptSaving = ref(false)
const resourceSaving = ref(false)
const resourceSelectedFile = ref(null)
const promptArgumentItems = ref([])
const toolDebugParams = ref('{\n  "pageNum": 1,\n  "pageSize": 10\n}')
const toolDebugResult = ref(null)
const toolDebugLoading = ref(false)
const toolSaving = ref(false)
const lastAutoToolScript = ref('')
const lastAutoToolSchema = ref('')
const communityTools = ref([])
const communityBuiltinTools = ref([])
const communityApis = ref([])
const communityKeyword = ref('')
const communityToolTypeFilter = ref('all')
const builtinTools = [
  { name: 'hello', description: '问候工具' },
  { name: 'current_time', description: '当前时间' },
  { name: 'system_info', description: '系统信息' },
  { name: 'calculate', description: '数学计算' },
  { name: 'create_request_config', description: '创建 API 请求配置' },
  { name: 'list_request_configs', description: '查询 API 请求配置' },
  { name: 'create_dynamic_tool', description: '创建动态 Tool 草稿' },
  { name: 'list_dynamic_tools', description: '查询动态 Tool 配置' },
  { name: 'update_dynamic_tool_script', description: '更新动态 Tool 脚本' },
  { name: 'create_resource', description: '直接创建或更新 Markdown Resource' },
  { name: 'create_prompt', description: '直接创建或更新 MCP Prompt 模板' },
  { name: 'create_skill', description: '直接创建或更新 Bear Skill' },
  { name: 'list_data_sources', description: '查询已发布数据源' },
  { name: 'query_data_source', description: '只读查询数据源' },
  { name: 'get_skill', description: '按 Skill ID 获取可安装的 Cursor Skill 文件' }
]
const tokenPermissionOptions = [
  { label: '工具列表 + 工具调用', value: '["mcp:tools:read","mcp:tools:call"]' },
  { label: '仅查看工具列表', value: '["mcp:tools:read"]' }
]
const activeStatusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]
const requestTypeOptions = [
  { label: 'HTTP', value: 'HTTP' }
]
const httpMethodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' }
]
const publishStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '上架不公开', value: 1 },
  { label: '公开', value: 2 }
]
const dataSourceDbTypeOptions = [
  { label: 'MYSQL', value: 'MYSQL' },
  { label: 'TIDB', value: 'TIDB' }
]
const dataSourcePublishStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 }
]
const menu = [
  ['dashboard', '概览', AppstoreOutlined], ['users', '用户', TeamOutlined], ['roles', '角色与能力权限', TeamOutlined], ['tokens', 'Token 与能力选择', KeyOutlined],
  ['requests', '请求配置', SettingOutlined], ['dataSources', '数据源', DatabaseOutlined], ['resources', '资源', DatabaseOutlined], ['tools', '动态工具', ThunderboltOutlined], ['audits', '审计日志', AuditOutlined]
]
const communityPages = ['shareHome', 'shareTools', 'shareApis']
const studioPages = ['studioHome', 'studioSkills', 'studioSkillEdit', 'studioTools', 'studioToolEdit', 'studioPrompts', 'studioPromptEdit', 'studioResources', 'studioResourceEdit', 'studioApis', 'studioApiEdit']
const sharePages = [...communityPages, ...studioPages]
const isSharePage = computed(() => sharePages.includes(page.value))
const isCommunityPage = computed(() => communityPages.includes(page.value))
const navSections = { dashboard: '概览', users: '系统治理', roles: '系统治理', tokens: '访问控制', requests: '能力展示', dataSources: '能力展示', resources: '能力展示', tools: '能力展示', audits: '运行观测', shareHome: '首页', shareTools: 'MCP Tools', shareApis: 'API 能力', studioHome: '首页', studioSkills: 'Skills 创作', studioTools: 'Tools 创作', studioPrompts: 'Prompts 创作', studioResources: 'Resources 创作', studioApis: 'API 创作' }
const title = computed(() => ({ dashboard:'运行概览', users:'用户', roles:'角色与能力权限', tokens:'Token 与能力选择', requests:'请求配置', dataSources:'数据源', resources:'资源', tools:'动态工具', audits:'调用审计', shareHome:'发现优质 AI 能力', shareTools:'MCP Tools', shareApis:'API 能力', studioHome:'Bear 创作空间', studioSkills:'Skills 创作', studioSkillEdit:'新建 Skill', studioTools:'Tools 创作', studioToolEdit:'新建 Tool', studioPrompts:'Prompts 创作', studioPromptEdit:'新建 Prompt', studioResources:'Resources 创作', studioResourceEdit:'新建 Resource', studioApis:'API 创作', studioApiEdit:'新建 API' })[page.value])
const desc = computed(() => ({ dashboard:'当前数据库中的 MCP 治理状态', users:'角色是工具和 Prompt 权限上限，用户通过角色获得资格', roles:'角色决定资格上限，Tool、Prompt、Resource 都在这里授权', tokens:'每把 Token 单独选择要暴露和实际允许使用的 Tool / Prompt / Resource', requests:'展示动态工具可引用的企业请求配置；完整创作在创作空间完成', dataSources:'维护外部数据库连接配置，为后续 query_data_source 和动态 Tool runSql 提供受控数据入口', resources:'展示已创建的 MCP Resource，发布并授权后可通过 resources/list 和 resources/read 读取', tools:'这里只展示已发布的动态工具；创建与编辑在创作空间完成', audits:'保留每一次 MCP 工具调用的结果摘要与耗时', shareHome:'公开的 Tool 和 API 会先进入社区，被团队发现、复用，再进入 Token 配置链路。', shareTools:'浏览已公开的 MCP Tool。能否调用仍由角色权限和 Token 工具选择决定。', shareApis:'浏览已公开的 API 配置，它们是动态 Tool 编排时可复用的基础能力。', studioHome:'创作 Skills、Tools、Prompts、Resources、API，分享到社区', studioSkills:'上传 Markdown Skill，让 Agent 通过 get_skill 安装到 Cursor 本地', studioSkillEdit:'上传 SKILL.md 并维护 Skill ID、名称、描述和发布状态', studioTools:'把已接入的 API 配置包装成 AI Agent 可见和可调用的 MCP Tool', studioToolEdit:'编写工具描述、入参 Schema 和 Groovy 脚本，调试通过后发布上线', studioPrompts:'沉淀企业工作流模板，指导 Agent 按标准流程使用工具', studioPromptEdit:'编写 Prompt 模板、参数和建议工具，预览渲染后发布', studioResources:'上传 Markdown 资源，让 Agent 通过 resources/list 和 resources/read 读取企业知识片段', studioResourceEdit:'上传 Markdown 文件并维护 Resource URI、名称、描述和发布状态', studioApis:'创建外部 HTTP API 配置，调试通过后发布给后续动态工具使用', studioApiEdit:'配置外部 HTTP API，保存并调试真实响应' })[page.value])
const studioApiStats = computed(() => {
  const all = rows.value.length
  const online = rows.value.filter(item => Number(item.publishStatus) !== 0).length
  const draft = rows.value.filter(item => Number(item.publishStatus) === 0).length

  return { all, online, draft }
})
const debugSummary = computed(() => {
  if (!debugResult.value) {
    return null
  }

  return {
    success: Boolean(debugResult.value.success),
    status: debugResult.value.result?.status,
    durationMs: debugResult.value.durationMs,
    errorMessage: debugResult.value.errorMessage
  }
})
const debugBodyText = computed(() => {
  if (!debugResult.value) {
    return '点击「发送」后显示响应体'
  }
  if (!debugResult.value.success) {
    return debugResult.value.errorMessage || '调试失败，请检查请求配置'
  }

  const body = debugResult.value.result?.body
  if (body == null || body === '') {
    return '响应体为空'
  }

  return prettyJsonText(body)
})
const filteredStudioApis = computed(() => {
  const keyword = apiKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      apiStatusFilter.value === 'all'
      || (apiStatusFilter.value === 'online' && publishStatus !== 0)
      || (apiStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.requestId,
      item.configKey,
      item.name,
      item.url,
      item.description
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioTools = computed(() => {
  const keyword = toolKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      toolStatusFilter.value === 'all'
      || (toolStatusFilter.value === 'online' && publishStatus !== 0)
      || (toolStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.toolName,
      item.toolDescription,
      item.linkedRequestKeys,
      item.groovyScript
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioPrompts = computed(() => {
  const keyword = promptKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      promptStatusFilter.value === 'all'
      || (promptStatusFilter.value === 'online' && publishStatus !== 0)
      || (promptStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.promptName,
      item.title,
      item.description,
      item.templateContent,
      item.linkedToolNames
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioSkills = computed(() => {
  const keyword = skillKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      skillStatusFilter.value === 'all'
      || (skillStatusFilter.value === 'online' && publishStatus !== 0)
      || (skillStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.skillCode,
      item.name,
      item.description,
      item.category,
      item.fileName,
      item.objectKey
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioResources = computed(() => {
  const keyword = resourceKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      resourceStatusFilter.value === 'all'
      || (resourceStatusFilter.value === 'online' && publishStatus !== 0)
      || (resourceStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.resourceUri,
      item.name,
      item.description,
      item.fileName,
      item.objectKey
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const communityDynamicToolSource = computed(() => {
  const source = page.value === 'shareTools' ? rows.value : communityTools.value
  return source.map(item => ({
    ...item,
    communityType: 'dynamic',
    displayName: item.toolName,
    displayDescription: item.toolDescription,
    displayCategory: '动态工具',
    scriptLanguage: item.scriptLanguage || 'Groovy'
  }))
})
const communityBuiltinToolSource = computed(() => {
  return communityBuiltinTools.value.map(item => ({
    ...item,
    linkedRequestKeys: '[]',
    communityType: 'builtin',
    displayName: item.toolName,
    displayDescription: item.toolDescription,
    displayCategory: item.category,
    scriptLanguage: item.scriptLanguage || 'Java @Tool'
  }))
})
const filteredCommunityTools = computed(() => {
  const source = [
    ...communityDynamicToolSource.value,
    ...communityBuiltinToolSource.value
  ].filter(item => {
    return communityToolTypeFilter.value === 'all' || item.communityType === communityToolTypeFilter.value
  })
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.displayName,
      item.displayDescription,
      item.displayCategory,
      item.linkedRequestKeys,
      item.inputSchema
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const filteredCommunityApis = computed(() => {
  const source = page.value === 'shareApis' ? rows.value : communityApis.value
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.requestId,
      item.configKey,
      item.name,
      item.description,
      item.category,
      item.method
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const communityDetailTitle = computed(() => {
  if (!activeCommunityItem.value) {
    return '能力详情'
  }
  if (activeCommunityItem.value.communityKind === 'api') {
    return `API 能力详情 · ${activeCommunityItem.value.configKey || activeCommunityItem.value.name || ''}`
  }
  return `MCP Tool 详情 · ${activeCommunityItem.value.displayName || activeCommunityItem.value.toolName || ''}`
})
const selectedToolRequestKeys = computed({
  get() {
    return parseJsonArray(model.value.linkedRequestKeys)
  },
  set(value) {
    model.value.linkedRequestKeys = JSON.stringify(value, null, 2)
  }
})
const availableToolApis = computed(() => {
  return requestConfigs.value.filter(item => Number(item.publishStatus) !== 0)
})
const toolApiOptions = computed(() => {
  return availableToolApis.value.map(item => ({
    label: `${item.configKey} ${item.name || ''}`,
    value: item.configKey,
    title: item.url || item.description || item.name || item.configKey
  }))
})
const selectedToolDataSourceIds = computed({
  get() {
    return parseJsonArray(model.value.linkedDataSourceIds).map(Number).filter(Number.isFinite)
  },
  set(value) {
    model.value.linkedDataSourceIds = JSON.stringify(value.map(Number), null, 2)
  }
})
const availableToolDataSources = computed(() => {
  return dataSourceConfigs.value.filter(item => Number(item.publishStatus) === 1)
})
const toolDataSourceOptions = computed(() => {
  return availableToolDataSources.value.map(item => ({
    label: `${item.id} ${item.name || item.datasourceKey || ''}`,
    value: item.id,
    title: item.description || item.jdbcUrl || item.name || String(item.id)
  }))
})
const selectedPromptToolNames = computed({
  get() {
    return parseJsonArray(model.value.linkedToolNames)
  },
  set(value) {
    model.value.linkedToolNames = JSON.stringify(value, null, 2)
  }
})
const promptToolOptions = computed(() => {
  const dynamicOptions = dynamicToolOptions.value.map(item => ({
    label: `${item.toolName} 动态工具`,
    value: item.toolName,
    title: item.toolDescription || item.toolName
  }))
  const builtinOptions = builtinTools.map(item => ({
    label: `${item.name} 内置工具`,
    value: item.name,
    title: item.description
  }))
  return [...dynamicOptions, ...builtinOptions]
})
const toolDebugText = computed(() => {
  if (!toolDebugResult.value) {
    return '点击「运行调试」后显示脚本返回结果'
  }

  return JSON.stringify(normalizeDebugResult(toolDebugResult.value), null, 2)
})
const drawerTitle = computed(() => {
  if (page.value === 'studioApis') {
    return model.value.id ? '编辑 HTTP API' : '新建 HTTP API'
  }
  return `${model.value.id ? '编辑' : '新建'}${title.value.replace('与工具权限','').replace('与工具选择','').replace('与能力权限','').replace('与能力选择','')}`
})
const userOptions = computed(() => users.value.map(user => ({
  label: `${user.displayName || user.username}（${user.username}）`,
  value: user.id
})))
const columns = computed(() => ({
 users:[['username','用户名'],['displayName','展示名称'],['roleCodes','角色'],['isEnabled','状态']],
 tokens:[['tokenName','Token 名称'],['userId','所属用户'],['permissions','访问范围'],['tokenPrefix','展示前缀'],['isActive','状态'],['expireTime','过期时间']],
 requests:[['requestId','接口 ID'],['configKey','配置 Key'],['name','名称'],['type','协议'],['publishStatus','发布'],['isEnabled','状态']],
 dataSources:[['name','名称'],['datasourceKey','数据源 Key'],['dbType','类型'],['jdbcUrl','JDBC URL'],['username','用户名'],['passwordSet','密码'],['publishStatus','发布']],
 resources:[['resourceUri','Resource URI'],['name','名称'],['description','描述'],['mimeType','MIME 类型'],['publishStatus','发布'],['enabled','状态']],
 tools:[['toolName','工具名'],['toolDescription','工具描述'],['linkedRequestKeys','API 白名单'],['linkedDataSourceIds','数据源白名单'],['publishStatus','发布'],['inputSchema','入参 Schema'],['groovyScript','脚本摘要'],['enabled','状态']],
 audits:[['createTime','调用时间'],['userName','用户'],['toolName','工具'],['status','状态'],['durationMs','耗时(ms)']],
 roles:[['roleCode','角色编码'],['roleName','角色名称'],['isEnabled','状态']]
})[page.value] || [])
const dataColumns = computed(() => columns.value.map(([dataIndex,title]) => ({
  title,
  dataIndex,
  ellipsis:true,
  customRender: ({ text, record }) => formatTableCell(dataIndex, text, record)
})))

function pageFromPath() {
  const path = window.location.pathname
  if (path === '/share/studio/skills') {
    return 'studioSkills'
  }
  if (path === '/share/studio/skills/edit') {
    return 'studioSkillEdit'
  }
  if (path === '/share/studio/tools') {
    return 'studioTools'
  }
  if (path === '/share/studio/tools/edit') {
    return 'studioToolEdit'
  }
  if (path === '/share/studio/prompts') {
    return 'studioPrompts'
  }
  if (path === '/share/studio/prompts/edit') {
    return 'studioPromptEdit'
  }
  if (path === '/share/studio/resources') {
    return 'studioResources'
  }
  if (path === '/share/studio/resources/edit') {
    return 'studioResourceEdit'
  }
  if (path === '/share/studio/apis') {
    return 'studioApis'
  }
  if (path === '/share/studio/apis/edit') {
    return 'studioApiEdit'
  }
  if (path === '/share/tools') {
    return 'shareTools'
  }
  if (path === '/share/apis') {
    return 'shareApis'
  }
  if (path === '/share/studio') {
    return 'studioHome'
  }
  if (path === '/share') {
    return 'shareHome'
  }
  return 'dashboard'
}

function pathForPage(key) {
  return {
    shareHome: '/share',
    shareTools: '/share/tools',
    shareApis: '/share/apis',
    studioHome: '/share/studio',
    studioSkills: '/share/studio/skills',
    studioSkillEdit: '/share/studio/skills/edit',
    studioTools: '/share/studio/tools',
    studioToolEdit: '/share/studio/tools/edit',
    studioPrompts: '/share/studio/prompts',
    studioPromptEdit: '/share/studio/prompts/edit',
    studioResources: '/share/studio/resources',
    studioResourceEdit: '/share/studio/resources/edit',
    studioApis: '/share/studio/apis',
    studioApiEdit: '/share/studio/apis/edit',
    dashboard: '/admin'
  }[key] || '/admin'
}

function formatTableCell(dataIndex, text, record) {
  if (dataIndex === 'roleCodes' && page.value === 'users') {
    return userRoleLabel(record.id)
  }
  if (dataIndex === 'userId' && page.value === 'tokens') {
    return userLabel(text)
  }
  if (dataIndex === 'permissions' && page.value === 'tokens') {
    return permissionLabel(text)
  }
  if (dataIndex === 'linkedRequestKeys' && page.value === 'tools') {
    return linkedRequestLabel(text)
  }
  if (dataIndex === 'linkedDataSourceIds' && page.value === 'tools') {
    return linkedDataSourceLabel(text)
  }
  if (dataIndex === 'inputSchema' && page.value === 'tools') {
    return compactText(text, 42)
  }
  if (dataIndex === 'groovyScript' && page.value === 'tools') {
    return compactText(text, 48)
  }
  if (dataIndex === 'jdbcUrl' && page.value === 'dataSources') {
    return compactText(text, 56)
  }
  if (dataIndex === 'passwordSet' && page.value === 'dataSources') {
    return text ? '已设置' : '未设置'
  }
  if (dataIndex === 'publishStatus' && page.value === 'resources') {
    return publishLabel(text)
  }
  if (dataIndex === 'enabled' && page.value === 'resources') {
    return Number(text) === 1 ? '启用' : '禁用'
  }
  if (dataIndex === 'isEnabled' || dataIndex === 'isActive' || dataIndex === 'enabled') {
    return Number(text) === 1 ? '启用' : '禁用'
  }
  if (dataIndex === 'publishStatus') {
    if (page.value === 'dataSources') {
      return Number(text) === 1 ? '已发布' : '草稿'
    }
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

function permissionLabel(value) {
  const normalized = normalizePermissions(value)
  const option = tokenPermissionOptions.find(item => item.value === normalized)
  return option?.label || '未配置'
}

function linkedRequestLabel(value) {
  const keys = parseJsonArray(value)
  if (!keys.length) {
    return '未绑定'
  }

  return keys.map(key => {
    const config = requestConfigs.value.find(item => item.configKey === key)
    return config ? `${key}（${config.name}）` : key
  }).join('，')
}

function linkedDataSourceLabel(value) {
  const ids = parseJsonArray(value).map(Number).filter(Number.isFinite)
  if (!ids.length) {
    return '未绑定'
  }

  return ids.map(id => {
    const dataSource = dataSourceConfigs.value.find(item => Number(item.id) === id)
    return dataSource ? `${id}（${dataSource.name || dataSource.datasourceKey}）` : String(id)
  }).join('，')
}

function parseJsonArray(value) {
  if (!value) {
    return []
  }
  if (Array.isArray(value)) {
    return value
  }
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return String(value).split(',').map(item => item.trim()).filter(Boolean)
  }
}

function compactText(value, maxLength) {
  if (!value) {
    return '-'
  }
  const text = String(value).replace(/\s+/g, ' ').trim()
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

function communityCode(prefix, id) {
  const value = id == null ? 0 : Number(id)
  return `${prefix}${String(value).padStart(10, '0')}`
}

function communityToolCode(item) {
  return item.communityType === 'builtin'
    ? item.toolName
    : communityCode('TOOL', item.id)
}

function communityToolCountLabel(item) {
  if (item.communityType === 'builtin') {
    return item.displayCategory || '内置工具'
  }
  return `${parseJsonArray(item.linkedRequestKeys).length} 个 API · ${parseJsonArray(item.linkedDataSourceIds).length} 个数据源`
}

function openCommunityToolDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'tool'
  }
  communityDetailOpen.value = true
}

function openCommunityApiDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'api'
  }
  communityDetailOpen.value = true
}

function prettyJsonText(value) {
  if (typeof value !== 'string') {
    return JSON.stringify(value, null, 2)
  }

  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch (error) {
    return value
  }
}

function normalizeDebugResult(value) {
  if (Array.isArray(value)) {
    return value.map(item => normalizeDebugResult(item))
  }
  if (value && typeof value === 'object') {
    const result = {}
    Object.entries(value).forEach(([key, item]) => {
      if (key === 'body' && typeof item === 'string') {
        result[key] = parseMaybeJson(item)
      } else {
        result[key] = normalizeDebugResult(item)
      }
    })
    return result
  }
  if (typeof value === 'string') {
    return parseMaybeJson(value)
  }
  return value
}

function parseMaybeJson(value) {
  const text = value.trim()
  if (!text) {
    return value
  }
  if (!['{', '['].includes(text[0])) {
    return value
  }

  try {
    return normalizeDebugResult(JSON.parse(text))
  } catch (error) {
    return value
  }
}

function userRoleLabel(userId) {
  const codes = userRoles.value
    .filter(item => item.userId === userId)
    .map(item => item.roleCode)

  if (!codes.length) {
    return '未分配'
  }

  return codes.map(code => {
    const role = roles.value.find(item => item.roleCode === code)
    return role ? `${role.roleName}（${code}）` : code
  }).join('，')
}

async function load() {
  loading.value = true
  try {
    if (page.value === 'shareHome') {
      rows.value = []
      [communityTools.value, communityBuiltinTools.value, communityApis.value] = await Promise.all([
        api('/api/share/community/tools'),
        api('/api/share/community/builtin-tools'),
        api('/api/share/community/apis')
      ])
    }
    else if (page.value === 'shareTools') {
      [rows.value, communityBuiltinTools.value] = await Promise.all([
        api('/api/share/community/tools'),
        api('/api/share/community/builtin-tools')
      ])
    }
    else if (page.value === 'shareApis') {
      rows.value = await api('/api/share/community/apis')
    }
    else if (page.value === 'studioHome') rows.value = await api('/api/share/studio/apis')
    else if (page.value === 'studioSkills') {
      rows.value = await api('/api/share/studio/skills')
    }
    else if (page.value === 'studioSkillEdit') {
      if (!model.value.skillCode) {
        model.value = emptySkillModel()
      }
    }
    else if (page.value === 'dashboard') { dashboard.value = await api('/dashboard'); rows.value = dashboard.value.recentAudits || [] }
    else if (page.value === 'users') {
      [rows.value, roles.value, userRoles.value] = await Promise.all([
        api('/users'),
        api('/roles'),
        api('/users/role-relations')
      ])
      users.value = rows.value
    }
    else if (page.value === 'roles') {
      [rows.value, roleTools.value, rolePrompts.value, roleResources.value, dynamicToolOptions.value, promptOptions.value, resourceOptions.value] = await Promise.all([
        api('/roles'),
        api('/role-tools'),
        api('/role-prompts'),
        api('/role-resources'),
        api('/dynamic-tools'),
        api('/prompt-templates'),
        api('/resources')
      ])
    }
    else if (page.value === 'tokens') {
      [rows.value, users.value, tokenSelections.value, tokenPromptSelections.value, tokenResourceSelections.value, dynamicToolOptions.value, promptOptions.value, resourceOptions.value] = await Promise.all([
        api('/tokens'),
        api('/users'),
        api('/token-selections'),
        api('/token-prompt-selections'),
        api('/token-resource-selections'),
        api('/dynamic-tools'),
        api('/prompt-templates'),
        api('/resources')
      ])
    }
    else if (page.value === 'requests') rows.value = await api('/request-configs')
    else if (page.value === 'dataSources') rows.value = await api('/data-sources')
    else if (page.value === 'resources') rows.value = await api('/resources')
    else if (page.value === 'studioTools') {
      [rows.value, requestConfigs.value, dataSourceConfigs.value] = await Promise.all([
        api('/api/share/studio/tools'),
        api('/api/share/studio/apis'),
        api('/data-sources')
      ])
    }
    else if (page.value === 'studioToolEdit') {
      [requestConfigs.value, dataSourceConfigs.value] = await Promise.all([
        api('/api/share/studio/apis'),
        api('/data-sources')
      ])
      if (!model.value.toolName) {
        model.value = emptyToolModel()
      }
    }
    else if (page.value === 'studioPrompts') {
      [rows.value, dynamicToolOptions.value] = await Promise.all([
        api('/api/share/studio/prompts'),
        api('/dynamic-tools')
      ])
    }
    else if (page.value === 'studioPromptEdit') {
      dynamicToolOptions.value = await api('/dynamic-tools')
      if (!model.value.promptName) {
        model.value = emptyPromptModel()
        syncPromptArgumentsFromModel()
        promptPreviewArgs.value = buildPromptPreviewArgs()
      }
    }
    else if (page.value === 'studioResources') {
      rows.value = await api('/api/share/studio/resources')
    }
    else if (page.value === 'studioResourceEdit') {
      if (!model.value.resourceUri) {
        model.value = emptyResourceModel()
      }
    }
    else if (page.value === 'studioApis') rows.value = await api('/api/share/studio/apis')
    else if (page.value === 'studioApiEdit') {
      if (!model.value.requestId) {
        model.value = emptyApiModel()
        apiEditorDebugParams.value = model.value.paramsDefault || '{}'
      }
    }
    else if (page.value === 'tools') {
      [rows.value, requestConfigs.value, dataSourceConfigs.value] = await Promise.all([
        api('/dynamic-tools'),
        api('/request-configs'),
        api('/data-sources')
      ])
    }
    else rows.value = await api('/audit-logs')
  } catch (error) {
    if (loggedIn.value) {
      console.error(error)
    }
  } finally { loading.value = false }
}
function changePage(key) {
  page.value = key
  rawToken.value = ''
  const nextPath = pathForPage(key)
  if (window.location.pathname !== nextPath) {
    window.history.pushState({}, '', nextPath)
  }
  load()
}
function syncPageFromLocation() {
  page.value = pageFromPath()
  load()
}
function emptyModel() {
  if (page.value === 'users') return { username:'', displayName:'', password:'', isEnabled:1 }
  if (page.value === 'roles') return { roleCode:'', roleName:'', description:'', isEnabled:1 }
  if (page.value === 'tokens') return { userId: users.value[0]?.id, tokenName:'新建 Token', permissions:'["mcp:tools:read","mcp:tools:call"]', isActive:1 }
  if (page.value === 'requests' || page.value === 'studioApis' || page.value === 'studioApiEdit') return emptyApiModel()
  if (page.value === 'dataSources') return emptyDataSourceModel()
  if (page.value === 'tools') return { toolName:'', toolDescription:'', inputSchema:'{"type":"object","properties":{}}', groovyScript:'return [message: params.message]', linkedRequestKeys:'[]', linkedDataSourceIds:'[]', enabled:1 }
  if (page.value === 'studioSkills' || page.value === 'studioSkillEdit') return emptySkillModel()
  if (page.value === 'studioPrompts' || page.value === 'studioPromptEdit') return emptyPromptModel()
  if (page.value === 'studioResources' || page.value === 'studioResourceEdit') return emptyResourceModel()
  return {}
}
function emptyApiModel() {
  return { requestId:'API' + Date.now(), configKey:'', name:'', type:'HTTP', method:'GET', url:'', headers:'{}', bodyTemplate:'', paramsDefault:'{}', connectTimeoutMs:5000, readTimeoutMs:15000, serviceName:'', methodName:'', argsSchema:'{}', creatorId: users.value[0]?.id, isEnabled:1, rateLimitPerMinute:0, publishStatus:0, description:'', category:'' }
}
function emptyDataSourceModel() {
  return { name:'', datasourceKey:'', dbType:'MYSQL', jdbcUrl:'jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai', username:'', password:'', extraJdbcProps:'{}', description:'', publishStatus:0 }
}
function emptySkillModel() {
  return {
    skillCode: '',
    name: '',
    description: '',
    category: '',
    objectKey: '',
    fileName: '',
    fileSize: null,
    enabled: 0,
    publishStatus: 0
  }
}
function emptyToolModel() {
  const script = defaultToolScript()
  const schema = defaultToolSchema()
  lastAutoToolScript.value = script
  lastAutoToolSchema.value = schema

  return {
    toolName: '',
    toolDescription: '',
    inputSchema: schema,
    groovyScript: script,
    linkedRequestKeys: '[]',
    linkedDataSourceIds: '[]',
    enabled: 0,
    publishStatus: 0
  }
}
function emptyPromptModel() {
  return {
    promptName: '',
    title: '',
    description: '',
    argumentsSchema: '[]',
    templateContent: defaultPromptTemplate(),
    linkedToolNames: '[]',
    enabled: 0,
    publishStatus: 0
  }
}
function emptyResourceModel() {
  return {
    resourceUri: '',
    name: '',
    description: '',
    mimeType: 'text/markdown',
    objectKey: '',
    fileName: '',
    fileSize: null,
    enabled: 0,
    publishStatus: 0
  }
}
function openCreate() {
  if (page.value === 'studioSkills') {
    openSkillEditor()
    return
  }
  if (page.value === 'studioApis') {
    openApiEditor()
    return
  }
  if (page.value === 'studioTools') {
    openToolEditor()
    return
  }
  if (page.value === 'studioPrompts') {
    openPromptEditor()
    return
  }
  if (page.value === 'studioResources') {
    openResourceEditor()
    return
  }
  model.value = emptyModel(); drawer.value=true
}
function openSkillEditor(row) {
  model.value = row ? { ...row } : emptySkillModel()
  skillSelectedFile.value = null
  skillPreviewText.value = ''
  page.value = 'studioSkillEdit'
  window.history.pushState({}, '', pathForPage('studioSkillEdit'))
  if (row?.id && row?.objectKey) {
    loadSkillPreview(row.id)
  }
}
function openToolEditor(row) {
  model.value = row ? { ...row } : emptyToolModel()
  if (!model.value.linkedDataSourceIds) {
    model.value.linkedDataSourceIds = '[]'
  }
  lastAutoToolScript.value = row ? '' : model.value.groovyScript
  lastAutoToolSchema.value = row ? '' : model.value.inputSchema
  toolDebugResult.value = null
  toolDebugParams.value = '{}'
  page.value = 'studioToolEdit'
  window.history.pushState({}, '', pathForPage('studioToolEdit'))
}
function openApiEditor(row) {
  model.value = row ? { ...row } : emptyApiModel()
  apiEditorDebugParams.value = model.value.paramsDefault || '{}'
  debugResult.value = null
  apiEditorTab.value = 'body'
  page.value = 'studioApiEdit'
  window.history.pushState({}, '', pathForPage('studioApiEdit'))
}
function openPromptEditor(row) {
  model.value = row ? { ...row } : emptyPromptModel()
  if (!model.value.linkedToolNames) {
    model.value.linkedToolNames = '[]'
  }
  if (!model.value.argumentsSchema) {
    model.value.argumentsSchema = '[]'
  }
  syncPromptArgumentsFromModel()
  promptPreviewResult.value = null
  promptPreviewArgs.value = buildPromptPreviewArgs()
  page.value = 'studioPromptEdit'
  window.history.pushState({}, '', pathForPage('studioPromptEdit'))
}
function openResourceEditor(row) {
  model.value = row ? { ...row } : emptyResourceModel()
  resourceSelectedFile.value = null
  resourcePreviewText.value = ''
  page.value = 'studioResourceEdit'
  window.history.pushState({}, '', pathForPage('studioResourceEdit'))
  if (row?.id && row?.objectKey) {
    loadResourcePreview(row.id)
  }
}
function edit(row) {
  model.value = { ...row }
  if (page.value === 'tokens') {
    model.value.permissions = normalizePermissions(model.value.permissions)
  }
  if (page.value === 'studioApis') {
    openApiEditor(row)
    return
  }
  if (page.value === 'studioSkills') {
    openSkillEditor(row)
    return
  }
  if (page.value === 'studioTools') {
    openToolEditor(row)
    return
  }
  if (page.value === 'studioPrompts') {
    openPromptEditor(row)
    return
  }
  if (page.value === 'studioResources') {
    openResourceEditor(row)
    return
  }
  drawer.value=true
}
async function save() {
  const base = { users:'/users', roles:'/roles', tokens:'/tokens', requests:'/request-configs', dataSources:'/data-sources', tools:'/dynamic-tools', studioApis:'/api/share/studio/apis' }[page.value]
  const method = model.value.id ? 'PUT' : 'POST'
  const body = buildSaveBody()
  const result = await api(model.value.id ? `${base}/${model.value.id}` : base, { method, body })
  if (page.value === 'tokens' && result?.rawToken) {
    rawToken.value = result.rawToken
    oneTimeTokenName.value = result.token?.tokenName || body.tokenName || '新建 Token'
    oneTimeTokenOpen.value = true
  }
  drawer.value = false; await load()
}

async function copyRawToken() {
  if (!rawToken.value) {
    return
  }
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(rawToken.value)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = rawToken.value
      textarea.setAttribute('readonly', 'readonly')
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    message.success('Token 已复制')
  } catch (error) {
    message.error('复制失败，请手动选中 Token 复制')
  }
}

function closeOneTimeTokenModal() {
  oneTimeTokenOpen.value = false
  rawToken.value = ''
  oneTimeTokenName.value = ''
}
async function removeRow(record) {
  if (page.value !== 'dataSources') {
    return
  }
  if (!window.confirm(`确定删除数据源「${record.name || record.datasourceKey}」？`)) {
    return
  }
  await api(`/data-sources/${record.id}`, { method: 'DELETE' })
  message.success('已删除数据源')
  await load()
}
async function testDataSourceConnection() {
  if (page.value !== 'dataSources') {
    return
  }
  dataSourceTesting.value = true
  try {
    const body = buildSaveBody()
    const url = model.value.id ? `/data-sources/${model.value.id}/test-connection` : '/data-sources/test-connection'
    await api(url, { method: 'POST', body })
    message.success('数据源连接成功')
  } finally {
    dataSourceTesting.value = false
  }
}
function buildSaveBody() {
  const body = { ...model.value }
  if (page.value === 'tokens' && !body.expireTime) {
    delete body.expireTime
  }
  if (page.value === 'tokens') {
    body.permissions = normalizePermissions(body.permissions)
  }
  if (page.value === 'users' && body.id) {
    delete body.password
  }
  if (page.value === 'dataSources') {
    delete body.passwordSet
    delete body.lastOperatorId
    if (body.id && !body.password) {
      delete body.password
    }
  }
  if (page.value === 'studioApis' || page.value === 'studioApiEdit') {
    body.type = 'HTTP'
    delete body.creatorId
    delete body.serviceName
    delete body.methodName
    delete body.argsSchema
    delete body.publishStatus
  }
  if (page.value === 'studioTools' || page.value === 'studioToolEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
    body.linkedDataSourceIds = body.linkedDataSourceIds || '[]'
  }
  if (page.value === 'studioSkills' || page.value === 'studioSkillEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
  }
  if (page.value === 'studioPrompts' || page.value === 'studioPromptEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
    syncPromptArgumentsToModel()
    body.argumentsSchema = model.value.argumentsSchema || '[]'
    body.linkedToolNames = body.linkedToolNames || '[]'
  }
  if (page.value === 'studioResources' || page.value === 'studioResourceEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
    body.mimeType = 'text/markdown'
  }
  return body
}

function defaultToolScript() {
  return 'def result = runRequest.runRequest("api_config_key", params)\n\nreturn [\n    message: "API调用完成",\n    requestParams: params,\n    result: result\n]'
}

function defaultPromptTemplate() {
  return '你是企业 AI 工作流助手。\n\n任务：围绕 {{topic}} 完成分析。\n\n建议流程：\n1. 先理解用户目标，确认缺失参数。\n2. 优先使用下方建议工具完成事实查询。\n3. 如果工具返回 rows，不要原样堆砌，先提炼关键结论。\n4. 最终输出：结论、依据、下一步建议。\n\n建议使用的工具：\n{{tools}}'
}

function renderMarkdown(markdown) {
  if (!markdown) {
    return '<p class="md-empty">选择 Markdown 文件后显示预览内容。</p>'
  }
  const lines = String(markdown).replace(/\r\n/g, '\n').split('\n')
  const html = []
  let paragraph = []
  let listType = ''
  let codeLines = []
  let inCode = false

  const flushParagraph = () => {
    if (!paragraph.length) return
    html.push(`<p>${renderInline(paragraph.join(' '))}</p>`)
    paragraph = []
  }
  const closeList = () => {
    if (!listType) return
    html.push(`</${listType}>`)
    listType = ''
  }

  lines.forEach(line => {
    const raw = line
    const trimmed = raw.trim()
    if (trimmed.startsWith('```')) {
      if (inCode) {
        html.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
        codeLines = []
        inCode = false
      } else {
        flushParagraph()
        closeList()
        inCode = true
      }
      return
    }
    if (inCode) {
      codeLines.push(raw)
      return
    }
    if (!trimmed) {
      flushParagraph()
      closeList()
      return
    }
    const heading = trimmed.match(/^(#{1,6})\s+(.+)$/)
    if (heading) {
      flushParagraph()
      closeList()
      const level = heading[1].length
      html.push(`<h${level}>${renderInline(heading[2])}</h${level}>`)
      return
    }
    const unordered = trimmed.match(/^[-*]\s+(.+)$/)
    if (unordered) {
      flushParagraph()
      if (listType !== 'ul') {
        closeList()
        listType = 'ul'
        html.push('<ul>')
      }
      html.push(`<li>${renderInline(unordered[1])}</li>`)
      return
    }
    const ordered = trimmed.match(/^\d+\.\s+(.+)$/)
    if (ordered) {
      flushParagraph()
      if (listType !== 'ol') {
        closeList()
        listType = 'ol'
        html.push('<ol>')
      }
      html.push(`<li>${renderInline(ordered[1])}</li>`)
      return
    }
    paragraph.push(trimmed)
  })
  if (inCode) {
    html.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
  }
  flushParagraph()
  closeList()
  return html.join('\n')
}

function renderInline(value) {
  return escapeHtml(value)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function defaultToolSchema() {
  return '{\n  "type": "object",\n  "properties": {\n    "pageNum": {\n      "type": "integer",\n      "description": "页码"\n    },\n    "pageSize": {\n      "type": "integer",\n      "description": "每页条数"\n    }\n  }\n}'
}

function promptArguments() {
  return promptArgumentItems.value
}

function promptArgumentKey() {
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function normalizePromptArgument(item, index) {
  return {
    _key: item?._key || promptArgumentKey(),
    name: item?.name || '',
    description: item?.description || `参数 ${index + 1}`,
    required: Boolean(item?.required),
    defaultValue: item?.defaultValue ?? ''
  }
}

function serializablePromptArguments() {
  return promptArgumentItems.value
    .filter(item => item && item.name)
    .map(({ name, description, required, defaultValue }) => ({
      name,
      description,
      required,
      defaultValue
    }))
}

function syncPromptArgumentsFromModel() {
  promptArgumentItems.value = parseJsonArray(model.value.argumentsSchema)
    .map((item, index) => normalizePromptArgument(item, index))
}

function syncPromptArgumentsToModel() {
  model.value.argumentsSchema = JSON.stringify(serializablePromptArguments(), null, 2)
}

function addPromptArgument() {
  const nextIndex = promptArgumentItems.value.length + 1
  promptArgumentItems.value.push({
    _key: promptArgumentKey(),
    name: `param${nextIndex}`,
    description: `参数 ${nextIndex}`,
    required: false,
    defaultValue: ''
  })
  syncPromptArgumentsToModel()
  promptPreviewArgs.value = buildPromptPreviewArgs()
}

function removePromptArgument(index) {
  promptArgumentItems.value.splice(index, 1)
  syncPromptArgumentsToModel()
  promptPreviewArgs.value = buildPromptPreviewArgs()
}

function updatePromptArgument(index, field, value) {
  if (!promptArgumentItems.value[index]) {
    return
  }
  promptArgumentItems.value[index][field] = value
  syncPromptArgumentsToModel()
  if (field === 'name' || field === 'defaultValue') {
    promptPreviewArgs.value = buildPromptPreviewArgs()
  }
}

function updatePromptArguments(args) {
  promptArgumentItems.value = args.map((item, index) => normalizePromptArgument(item, index))
  syncPromptArgumentsToModel()
}

function extractPromptArgumentsFromTemplate() {
  const template = model.value.templateContent || ''
  const args = promptArguments()
  const names = args.map(item => item.name)
  const pattern = /\{\{\s*([A-Za-z_][A-Za-z0-9_.-]*)\s*}}/g
  let match = pattern.exec(template)
  while (match) {
    const name = match[1]
    if (name !== 'tools' && !names.includes(name)) {
      args.push({
        name,
        description: name,
        required: true,
        defaultValue: ''
      })
      names.push(name)
    }
    match = pattern.exec(template)
  }
  updatePromptArguments(args)
  promptPreviewArgs.value = buildPromptPreviewArgs()
  message.success('已从模板提取参数')
}

function buildPromptPreviewArgs() {
  const params = {}
  serializablePromptArguments().forEach(item => {
    params[item.name] = item.defaultValue ?? ''
  })
  return JSON.stringify(params, null, 2)
}

function promptToolsText() {
  const names = selectedPromptToolNames.value
  if (!names.length) {
    return '{{tools}}'
  }
  return names.map(name => `- ${name}`).join('\n')
}

function promptToolsBlock() {
  return `建议使用的工具：\n${promptToolsText()}`
}

function syncPromptToolsIntoTemplate() {
  if (page.value !== 'studioPromptEdit') {
    return
  }

  const template = model.value.templateContent || ''
  const block = promptToolsBlock()
  const managedBlockPattern = /建议使用的工具：\n(?:- .*(?:\n|$)|\{\{tools}}(?:\n|$))*/m
  if (template.includes('{{tools}}')) {
    model.value.templateContent = template.replace(/\{\{tools}}/g, promptToolsText())
    return
  }
  if (managedBlockPattern.test(template)) {
    model.value.templateContent = template.replace(managedBlockPattern, block)
    return
  }
  if (selectedPromptToolNames.value.length) {
    model.value.templateContent = `${template.trimEnd()}\n\n${block}`
  }
}

function insertPromptToolsPlaceholder() {
  syncPromptToolsIntoTemplate()
}

function tryAutoGenerateToolAssets() {
  if (page.value !== 'studioToolEdit') {
    return
  }

  const script = model.value.groovyScript || ''
  if (!script.trim() || script === lastAutoToolScript.value || script === defaultToolScript()) {
    if (selectedToolRequestKeys.value.length) {
      generateToolScriptFromSelection()
    } else if (selectedToolDataSourceIds.value.length) {
      generateToolScriptFromDataSourceSelection()
    }
  }

  const schema = model.value.inputSchema || ''
  if (!schema.trim() || schema === lastAutoToolSchema.value || schema === defaultToolSchema()) {
    if (selectedToolRequestKeys.value.length) {
      generateToolSchemaFromSelection()
    } else if (selectedToolDataSourceIds.value.length) {
      generateToolSchemaFromScript()
    }
  }
}

function generateToolScriptFromSelection() {
  const keys = selectedToolRequestKeys.value
  if (!keys.length) {
    return
  }

  const apiConfigs = keys.map(key => {
    return requestConfigs.value.find(item => item.configKey === key) || { configKey: key, name: key, paramsDefault: '{}' }
  })
  const script = buildToolScript(apiConfigs)
  model.value.groovyScript = script
  lastAutoToolScript.value = script
}

function generateToolSchemaFromSelection() {
  const keys = selectedToolRequestKeys.value
  if (!keys.length) {
    return
  }

  const apiConfigs = keys.map(key => {
    return requestConfigs.value.find(item => item.configKey === key) || { configKey: key, paramsDefault: '{}' }
  })
  const schema = buildToolSchema(apiConfigs)
  model.value.inputSchema = schema
  lastAutoToolSchema.value = schema
}

function generateToolAssetsFromSelection() {
  generateToolSchemaFromSelection()
  generateToolScriptFromSelection()
  generateToolDebugParamsFromSelection()
}

function generateToolScriptFromDataSourceSelection() {
  const ids = selectedToolDataSourceIds.value
  if (!ids.length) {
    return
  }

  const script = buildDataSourceToolScript(ids)
  model.value.groovyScript = script
  lastAutoToolScript.value = script
}

function generateToolAssetsFromDataSourceSelection() {
  generateToolScriptFromDataSourceSelection()
  generateToolSchemaFromScript()
}

function generateToolSchemaFromScript() {
  const entries = collectScriptParamEntries(model.value.groovyScript)
  if (!entries.length) {
    message.warning('没有识别到 params.xxx 参数')
    return
  }

  const currentSchema = parseJsonObject(model.value.inputSchema)
  const properties = {
    ...(currentSchema.properties && typeof currentSchema.properties === 'object' && !Array.isArray(currentSchema.properties)
      ? currentSchema.properties
      : {})
  }
  const required = new Set(Array.isArray(currentSchema.required) ? currentSchema.required : [])

  entries.forEach(item => {
    properties[item.key] = properties[item.key] || {
      type: item.type,
      description: item.description
    }
    if (item.required) {
      required.add(item.key)
    }
  })

  const nextSchema = {
    type: 'object',
    properties
  }
  if (required.size) {
    nextSchema.required = Array.from(required)
  }

  model.value.inputSchema = JSON.stringify(nextSchema, null, 2)
  toolDebugParams.value = JSON.stringify(buildScriptDebugParams(entries), null, 2)
  message.success(`已生成 ${entries.length} 个入参`)
}

function generateToolDebugParamsFromSelection() {
  const keys = selectedToolRequestKeys.value
  if (!keys.length) {
    toolDebugParams.value = '{}'
    return
  }

  const apiConfigs = keys.map(key => {
    return requestConfigs.value.find(item => item.configKey === key) || { configKey: key, paramsDefault: '{}', bodyTemplate: '' }
  })
  toolDebugParams.value = buildToolDebugParams(apiConfigs)
}

function buildDataSourceToolScript(ids) {
  const firstId = ids[0]
  const dataSource = dataSourceConfigs.value.find(item => Number(item.id) === Number(firstId))
  const name = dataSource?.name || dataSource?.datasourceKey || `数据源 ${firstId}`
  const runSqlCall = ids.length === 1
    ? 'runSql.runSql(sql)'
    : `runSql.runSql(${Number(firstId)}L, sql)`

  return `def limit = Math.min((params["limit"] ?: 10) as Integer, 100)

def sql = """
select 1 as demo_value
limit \${limit}
"""

def result = ${runSqlCall}

return [
    message: "${name} 查询完成",
    requestParams: [
        "limit": limit
    ],
    rows: result.rows,
    rowCount: result.row_count,
    truncated: result.truncated,
    result: result
]`
}

function buildToolScript(apiConfigs) {
  if (apiConfigs.length === 1) {
    return buildSingleApiToolScript(apiConfigs[0])
  }

  const calls = apiConfigs.map((apiConfig, index) => {
    const configKey = apiConfig.configKey
    const entries = collectApiParamEntries(apiConfig)
    const resultName = `result${index + 1}`

    if (!entries.length) {
      return `def ${resultName} = runRequest.runRequest("${configKey}", params)`
    }

    const requestLines = entries.map(item => {
      return `    "${item.key}": params["${item.key}"] ?: ${groovyLiteral(item.defaultValue)}`
    }).join(',\n')

    return `def ${resultName} = runRequest.runRequest("${configKey}", [\n${requestLines}\n])`
  }).join('\n\n')

  const resultLines = apiConfigs.map((apiConfig, index) => {
    return `    "${apiConfig.configKey}": result${index + 1}`
  }).join(',\n')

  return `${calls}\n\nreturn [\n    message: "组合调用完成",\n${resultLines}\n]`
}

function buildSingleApiToolScript(apiConfig) {
  const configKey = apiConfig.configKey
  const apiName = apiConfig.name || configKey
  const entries = collectApiParamEntries(apiConfig)

  if (!entries.length) {
    return `def result = runRequest.runRequest("${configKey}", params)\n\nreturn [\n    message: "${apiName}调用完成",\n    requestParams: params,\n    result: result\n]`
  }

  const requestLines = entries.map(item => {
    return `    "${item.key}": params["${item.key}"] ?: ${groovyLiteral(item.defaultValue)}`
  }).join(',\n')

  const responseLines = entries.map(item => {
    return `        "${item.key}": params["${item.key}"] ?: ${groovyLiteral(item.defaultValue)}`
  }).join(',\n')

  return `def result = runRequest.runRequest("${configKey}", [\n${requestLines}\n])\n\nreturn [\n    message: "${apiName}调用完成",\n    requestParams: [\n${responseLines}\n    ],\n    result: result\n]`
}

function buildToolSchema(apiConfigs) {
  const properties = {}

  apiConfigs.forEach(apiConfig => {
    collectApiParamEntries(apiConfig).forEach(item => {
      if (!properties[item.key]) {
        properties[item.key] = {
          type: item.type,
          description: item.description
        }
      }
    })
  })

  return JSON.stringify({
    type: 'object',
    properties
  }, null, 2)
}

function buildToolDebugParams(apiConfigs) {
  const params = {}

  apiConfigs.forEach(apiConfig => {
    collectApiParamEntries(apiConfig).forEach(item => {
      if (params[item.key] == null) {
        params[item.key] = item.defaultValue
      }
    })
  })

  return JSON.stringify(params, null, 2)
}

function collectScriptParamEntries(script) {
  if (!script) {
    return []
  }

  const text = String(script)
  const paramMap = new Map()
  const patterns = [
    /params\.([A-Za-z_][A-Za-z0-9_]*)/g,
    /params\[['"]([A-Za-z_][A-Za-z0-9_]*)['"]\]/g
  ]

  patterns.forEach(pattern => {
    let match = pattern.exec(text)
    while (match) {
      const key = match[1]
      if (key && key !== 'class') {
        const after = text.slice(match.index + match[0].length, match.index + match[0].length + 20)
        const optional = /^\s*\?:/.test(after)
        const existing = paramMap.get(key)
        paramMap.set(key, {
          key,
          type: inferScriptParamType(key),
          description: paramDescription(key),
          required: existing ? existing.required || !optional : !optional
        })
      }
      match = pattern.exec(text)
    }
  })

  return Array.from(paramMap.values())
}

function inferScriptParamType(key) {
  if (/^(pageNum|pageSize|limit|offset|count|size|num|id)$/i.test(key) || /(Id|Count|Num|Size|Limit|Offset)$/.test(key)) {
    return 'integer'
  }
  if (/^(is|has|enable|enabled|active)/i.test(key)) {
    return 'boolean'
  }
  return 'string'
}

function paramDescription(key) {
  const labels = {
    keyword: '关键词',
    name: '名称',
    pageNum: '页码',
    pageSize: '每页条数',
    limit: '最大返回条数',
    offset: '偏移量'
  }
  return labels[key] || key
}

function buildScriptDebugParams(entries) {
  const params = {}
  entries.forEach(item => {
    if (item.type === 'integer') {
      params[item.key] = item.key === 'pageNum' ? 1 : 10
    } else if (item.type === 'number') {
      params[item.key] = 1
    } else if (item.type === 'boolean') {
      params[item.key] = true
    } else {
      params[item.key] = ''
    }
  })
  return params
}

function collectApiParamEntries(apiConfig) {
  const paramsDefault = parseJsonObject(apiConfig.paramsDefault)
  const paramMap = new Map()

  Object.entries(paramsDefault).forEach(([key, value]) => {
    paramMap.set(key, {
      key,
      defaultValue: value,
      type: inferJsonSchemaType(value),
      description: key
    })
  })

  extractTemplateParams(apiConfig.bodyTemplate).forEach(key => {
    if (!paramMap.has(key)) {
      paramMap.set(key, {
        key,
        defaultValue: '',
        type: 'string',
        description: key
      })
    }
  })

  return Array.from(paramMap.values())
}

function extractTemplateParams(template) {
  if (!template) {
    return []
  }

  const keys = []
  const pattern = /\{\{\s*([A-Za-z0-9_.-]+)\s*\}\}/g
  let match = pattern.exec(String(template))
  while (match) {
    if (!keys.includes(match[1])) {
      keys.push(match[1])
    }
    match = pattern.exec(String(template))
  }
  return keys
}

function inferJsonSchemaType(value) {
  if (Number.isInteger(value)) {
    return 'integer'
  }
  if (typeof value === 'number') {
    return 'number'
  }
  if (typeof value === 'boolean') {
    return 'boolean'
  }
  if (Array.isArray(value)) {
    return 'array'
  }
  if (value && typeof value === 'object') {
    return 'object'
  }
  return 'string'
}

function parseJsonObject(value) {
  try {
    const parsed = value ? JSON.parse(value) : {}
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch (error) {
    return {}
  }
}

function groovyLiteral(value) {
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }
  if (value == null) {
    return 'null'
  }
  return JSON.stringify(String(value))
}

function normalizePermissions(value) {
  if (!value) {
    return '["mcp:tools:read","mcp:tools:call"]'
  }

  try {
    const permissions = Array.isArray(value) ? value : JSON.parse(value)
    if (permissions.includes('mcp:tools:call')) {
      return '["mcp:tools:read","mcp:tools:call"]'
    }
    if (permissions.includes('mcp:tools:read')) {
      return '["mcp:tools:read"]'
    }
  } catch (error) {
    console.warn('无法解析 Token 权限范围', value, error)
  }

  return String(value)
}
function uniqueValues(values) {
  return Array.from(new Set(values.filter(value => value !== undefined && value !== null && value !== '')))
}
function selectableToolNames() {
  const builtinNames = builtinTools.map(tool => tool.name)
  const dynamicNames = dynamicToolOptions.value
    .filter(tool => Number(tool.enabled) === 1)
    .map(tool => tool.toolName)
  return uniqueValues([...builtinNames, ...dynamicNames])
}
function selectablePromptNames() {
  return promptOptions.value
    .filter(prompt => Number(prompt.enabled) === 1 && Number(prompt.publishStatus) !== 0)
    .map(prompt => prompt.promptName)
}
function selectableResourceUris() {
  return resourceOptions.value
    .filter(resource => Number(resource.enabled) === 1 && Number(resource.publishStatus) !== 0)
    .map(resource => resource.resourceUri)
}
function selectAllRoleTools() {
  selectedRoleTools.value = selectableToolNames()
}
function selectAllTokenTools() {
  selectedTokenTools.value = selectableToolNames()
}
function selectAllRolePrompts() {
  selectedRolePrompts.value = selectablePromptNames()
}
function selectAllTokenPrompts() {
  selectedTokenPrompts.value = selectablePromptNames()
}
function selectAllRoleResources() {
  selectedRoleResources.value = selectableResourceUris()
}
function selectAllTokenResources() {
  selectedTokenResources.value = selectableResourceUris()
}
function clearRoleTools() {
  selectedRoleTools.value = []
}
function clearTokenTools() {
  selectedTokenTools.value = []
}
function clearRolePrompts() {
  selectedRolePrompts.value = []
}
function clearTokenPrompts() {
  selectedTokenPrompts.value = []
}
function clearRoleResources() {
  selectedRoleResources.value = []
}
function clearTokenResources() {
  selectedTokenResources.value = []
}
async function updateSelections(token) {
  activeToken.value = token
  selectedTokenTools.value = tokenSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.toolName)
  tokenSelectionOpen.value = true
}
async function updateTokenPromptSelections(token) {
  activeToken.value = token
  selectedTokenPrompts.value = tokenPromptSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.promptName)
  tokenPromptSelectionOpen.value = true
}
async function updateTokenResourceSelections(token) {
  activeToken.value = token
  selectedTokenResources.value = tokenResourceSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.resourceUri)
  tokenResourceSelectionOpen.value = true
}
async function updateUserRoles(user) {
  activeUser.value = user
  selectedUserRoles.value = userRoles.value
    .filter(item => item.userId === user.id)
    .map(item => item.roleCode)
  userRoleOpen.value = true
}
async function saveUserRoles() {
  await api(`/users/${activeUser.value.id}/roles`, {
    method: 'PUT',
    body: { codes: selectedUserRoles.value }
  })

  userRoleOpen.value = false
  await load()
}
async function updateRoleTools(role) {
  activeRole.value = role
  selectedRoleTools.value = roleTools.value.filter(item => item.roleCode === role.roleCode).map(item => item.toolName)
  toolPermissionOpen.value = true
}
async function updateRolePrompts(role) {
  activeRole.value = role
  selectedRolePrompts.value = rolePrompts.value.filter(item => item.roleCode === role.roleCode).map(item => item.promptName)
  promptPermissionOpen.value = true
}
async function updateRoleResources(role) {
  activeRole.value = role
  selectedRoleResources.value = roleResources.value.filter(item => item.roleCode === role.roleCode).map(item => item.resourceUri)
  resourcePermissionOpen.value = true
}
async function saveRoleTools() {
  await api(`/roles/${activeRole.value.roleCode}/tools`, { method:'PUT', body:{ codes:selectedRoleTools.value } })
  toolPermissionOpen.value = false
  await load()
}
async function saveRolePrompts() {
  await api(`/roles/${activeRole.value.roleCode}/prompts`, { method:'PUT', body:{ codes:selectedRolePrompts.value } })
  promptPermissionOpen.value = false
  await load()
}
async function saveRoleResources() {
  await api(`/roles/${activeRole.value.roleCode}/resources`, { method:'PUT', body:{ codes:selectedRoleResources.value } })
  resourcePermissionOpen.value = false
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
async function saveTokenPromptSelections() {
  const prompts = selectedTokenPrompts.value.map(promptName => ({
    promptName,
    enabled: 1
  }))

  await api(`/tokens/${activeToken.value.id}/prompt-selections`, {
    method: 'PUT',
    body: { prompts }
  })

  tokenPromptSelectionOpen.value = false
  await load()
}
async function saveTokenResourceSelections() {
  const resources = selectedTokenResources.value.map(resourceUri => ({
    resourceUri,
    enabled: 1
  }))

  await api(`/tokens/${activeToken.value.id}/resource-selections`, {
    method: 'PUT',
    body: { resources }
  })

  tokenResourceSelectionOpen.value = false
  await load()
}
async function toggleResourceEnabled(resource) {
  const enabled = Number(resource.enabled) === 1
  await api(`/resources/${resource.id}/${enabled ? 'disable' : 'enable'}`, { method: 'POST' })
  message.success(enabled ? 'Resource 已禁用' : 'Resource 已启用')
  await load()
}
function showDynamicToolDetail(tool) {
  activeDynamicTool.value = tool
  dynamicToolDetailOpen.value = true
}
function showAuditDetail(log) {
  activeAuditLog.value = log
  auditDetailOpen.value = true
}
function publishLabel(status) {
  return Number(status) === 0 ? '草稿' : '已上线'
}
function publishClass(status) {
  return Number(status) === 0 ? 'draft' : 'online'
}
function visibilityLabel(status) {
  return Number(status) === 2 ? '公开' : '不公开'
}
function visibilityClass(status) {
  return Number(status) === 2 ? 'public' : 'private'
}
function onlineActionLabel(status) {
  return Number(status) === 0 ? '上线' : '下线'
}
function visibilityActionLabel(status) {
  return Number(status) === 2 ? '不公开' : '公开'
}
function openApiDebug(apiConfig) {
  activeStudioApi.value = apiConfig
  debugParams.value = apiConfig.paramsDefault || '{}'
  debugResult.value = null
  apiDebugOpen.value = true
}
async function runApiDebug() {
  debugLoading.value = true
  try {
    const params = debugParams.value ? JSON.parse(debugParams.value) : {}
    debugResult.value = await api(`/api/share/studio/apis/${activeStudioApi.value.id}/debug`, {
      method: 'POST',
      body: { params }
    })
    message.success('调试成功')
  } catch (error) {
    debugResult.value = error.data || { success: false, errorMessage: error.message }
    message.error(debugResult.value.errorMessage || error.message || '调试失败')
  } finally {
    debugLoading.value = false
  }
}
async function saveApiEditor() {
  apiSaving.value = true
  try {
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/apis/${model.value.id}` : '/api/share/studio/apis'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    message.success(model.value.id ? 'API 已保存' : 'API 已创建')
    return result
  } catch (error) {
    message.error(error.message || '保存失败')
    throw error
  } finally {
    apiSaving.value = false
  }
}
async function sendApiEditor() {
  debugLoading.value = true
  try {
    const params = apiEditorDebugParams.value ? JSON.parse(apiEditorDebugParams.value) : {}
    debugResult.value = await api('/api/share/studio/apis/debug', {
      method: 'POST',
      body: { ...buildSaveBody(), params }
    })
    message.success('调试成功')
  } catch (error) {
    debugResult.value = error.data || { success: false, errorMessage: error.message }
    message.error(debugResult.value.errorMessage || error.message || '调试失败')
  } finally {
    debugLoading.value = false
  }
}
async function saveToolEditor() {
  toolSaving.value = true
  try {
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/tools/${model.value.id}` : '/api/share/studio/tools'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    message.success(model.value.id ? 'Tool 已保存' : 'Tool 已创建')
    return result
  } catch (error) {
    message.error(error.message || '保存失败')
    throw error
  } finally {
    toolSaving.value = false
  }
}
async function sendToolEditor() {
  toolDebugLoading.value = true
  try {
    const params = toolDebugParams.value ? JSON.parse(toolDebugParams.value) : {}
    toolDebugResult.value = await api('/api/share/studio/tools/debug', {
      method: 'POST',
      body: { params, tool: buildSaveBody() }
    })
    message.success('调试成功')
  } catch (error) {
    toolDebugResult.value = error.data || { success: false, errorMessage: error.message }
    message.error(toolDebugResult.value.errorMessage || error.message || '调试失败')
  } finally {
    toolDebugLoading.value = false
  }
}
async function savePromptEditor() {
  promptSaving.value = true
  try {
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/prompts/${model.value.id}` : '/api/share/studio/prompts'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    message.success(model.value.id ? 'Prompt 已保存' : 'Prompt 已创建')
    return result
  } catch (error) {
    message.error(error.message || '保存失败')
    throw error
  } finally {
    promptSaving.value = false
  }
}

async function previewPromptEditor() {
  promptPreviewLoading.value = true
  try {
    const args = promptPreviewArgs.value ? JSON.parse(promptPreviewArgs.value) : {}
    promptPreviewResult.value = await api('/api/share/studio/prompts/debug', {
      method: 'POST',
      body: { arguments: args, prompt: buildSaveBody() }
    })
    message.success('预览已生成')
  } catch (error) {
    promptPreviewResult.value = { success: false, errorMessage: error.message }
    message.error(error.message || '预览失败')
  } finally {
    promptPreviewLoading.value = false
  }
}

async function publishPrompt(prompt) {
  const result = await api(`/api/share/studio/prompts/${prompt.id}/publish`, { method: 'POST' })
  if (model.value.id === prompt.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Prompt 已公开发布')
  await load()
}

async function publishPrivatePrompt(prompt) {
  const result = await api(`/api/share/studio/prompts/${prompt.id}/publish-private`, { method: 'POST' })
  if (model.value.id === prompt.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Prompt 已设为不公开')
  await load()
}

async function unpublishPrompt(prompt) {
  const result = await api(`/api/share/studio/prompts/${prompt.id}/unpublish`, { method: 'POST' })
  if (model.value.id === prompt.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Prompt 已下线')
  await load()
}

async function toggleOnlinePrompt(prompt) {
  if (Number(prompt.publishStatus) !== 0) {
    await unpublishPrompt(prompt)
    return
  }
  await publishPrivatePrompt(prompt)
}

async function toggleVisibilityPrompt(prompt) {
  if (Number(prompt.publishStatus) === 0) {
    message.warning('先上线，再设置公开范围')
    return
  }
  if (Number(prompt.publishStatus) === 2) {
    await publishPrivatePrompt(prompt)
    return
  }
  await publishPrompt(prompt)
}
async function onSkillFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    skillSelectedFile.value = null
    return
  }
  if (!file.name.toLowerCase().endsWith('.md')) {
    message.warning('Skill 当前只支持 Markdown (.md) 文件')
    event.target.value = ''
    skillSelectedFile.value = null
    return
  }
  skillSelectedFile.value = file
  model.value.fileName = file.name
  model.value.fileSize = file.size
  if (!model.value.name) {
    model.value.name = file.name.replace(/\.md$/i, '')
  }
  skillPreviewText.value = await file.text()
  applySkillFrontmatterHint(skillPreviewText.value)
}

function applySkillFrontmatterHint(markdown) {
  const match = String(markdown || '').replace(/\r\n/g, '\n').match(/^---\s*\n([\s\S]*?)\n---/)
  if (!match) {
    return
  }
  const fields = {}
  match[1].split('\n').forEach(line => {
    const item = line.match(/^([a-zA-Z0-9_-]+):\s*(.*)$/)
    if (item) {
      fields[item[1]] = item[2].replace(/^['"]|['"]$/g, '').trim()
    }
  })
  if (fields.name && !model.value.name) {
    model.value.name = fields.name
  }
  if (fields.description && !model.value.description) {
    model.value.description = fields.description
  }
}

function skillInstallBaseUrl() {
  const origin = window.location.origin.replace(/\/$/, '')
  if (window.location.hostname === 'localhost' && window.location.port === '5173') {
    return 'http://localhost:8090'
  }
  return origin
}

function skillCliInstallCommand(skillCode) {
  return `bear-skill install ${skillCode} --base-url ${skillInstallBaseUrl()}`
}

function skillInstallScriptUrl() {
  return `${skillInstallBaseUrl()}/api/public/bear-skill/install.sh`
}

function buildCursorSkillInstallPrompt(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  const cliCmd = skillCliInstallCommand(skillCode)
  return '请先检查是否已安装 bear-skill CLI（执行 which bear-skill 或 command -v bear-skill）。\n\n'
    + '若未安装，请执行以下命令安装（安装到 ~/.local/bin，无需 sudo）：\n'
    + `  curl -fsSL ${skillInstallScriptUrl()} | bash -s -- --cli-only\n\n`
    + `若已安装，则直接安装 ${skillCode} 技能：\n`
    + `  ${cliCmd}\n\n`
    + '安装完成后，请确认当前项目下存在 .cursor/skills/ 目录，并提示我重启或刷新 Cursor。'
}

function openCursorSkillInstall(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  if (!skillCode || Number(skill?.publishStatus ?? model.value.publishStatus) === 0) {
    message.warning('请先上线 Skill，再安装到 Cursor')
    return
  }
  const prompt = encodeURIComponent(buildCursorSkillInstallPrompt(skill))
  const deeplink = `cursor://anysphere.cursor-deeplink/prompt?text=${prompt}`
  try {
    window.location.href = deeplink
  } catch (error) {
    window.open(`https://cursor.com/link/prompt?text=${prompt}`, '_blank')
  }
}

function downloadSkillZip(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  if (!skillCode || Number(skill?.publishStatus ?? model.value.publishStatus) === 0) {
    message.warning('请先上线 Skill，再下载 ZIP')
    return
  }
  window.open(`${skillInstallBaseUrl()}/api/public/skills/${encodeURIComponent(skillCode)}/download`, '_blank')
}

async function loadSkillPreview(skillId = model.value.id) {
  if (!skillId) {
    skillPreviewText.value = ''
    return
  }
  skillPreviewLoading.value = true
  try {
    const presign = await api(`/api/share/studio/skills/${skillId}/presign-download`)
    const response = await fetch(presign.downloadUrl)
    if (!response.ok) {
      throw new Error(`Skill 读取失败：${response.status}`)
    }
    skillPreviewText.value = await response.text()
  } catch (error) {
    skillPreviewText.value = error.message || 'Skill 读取失败'
  } finally {
    skillPreviewLoading.value = false
  }
}

async function uploadSkillFileIfNeeded() {
  if (!skillSelectedFile.value) {
    return
  }
  const file = skillSelectedFile.value
  const presign = await api('/api/share/studio/skills/presign-upload', {
    method: 'POST',
    body: {
      skillCode: model.value.skillCode,
      fileName: file.name,
      fileSize: file.size
    }
  })
  const uploadResponse = await fetch(presign.uploadUrl, {
    method: 'PUT',
    body: file
  })
  if (!uploadResponse.ok) {
    throw new Error(`Skill 上传失败：${uploadResponse.status}`)
  }
  model.value.skillCode = presign.skillCode
  model.value.objectKey = presign.objectKey
  model.value.fileName = file.name
  model.value.fileSize = file.size
  skillSelectedFile.value = null
}

async function saveSkillEditor() {
  skillSaving.value = true
  try {
    await uploadSkillFileIfNeeded()
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/skills/${model.value.id}` : '/api/share/studio/skills'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    await loadSkillPreview(result.id)
    message.success(model.value.id ? 'Skill 已保存' : 'Skill 已创建')
    return result
  } catch (error) {
    message.error(error.message || '保存失败')
    throw error
  } finally {
    skillSaving.value = false
  }
}

async function publishSkill(skill) {
  const result = await api(`/api/share/studio/skills/${skill.id}/publish`, { method: 'POST' })
  if (model.value.id === skill.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Skill 已公开发布')
  await load()
}

async function publishPrivateSkill(skill) {
  const result = await api(`/api/share/studio/skills/${skill.id}/publish-private`, { method: 'POST' })
  if (model.value.id === skill.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Skill 已设为不公开')
  await load()
}

async function unpublishSkill(skill) {
  const result = await api(`/api/share/studio/skills/${skill.id}/unpublish`, { method: 'POST' })
  if (model.value.id === skill.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Skill 已下线')
  await load()
}

async function toggleOnlineSkill(skill) {
  if (Number(skill.publishStatus) !== 0) {
    await unpublishSkill(skill)
    return
  }
  await publishPrivateSkill(skill)
}

async function toggleVisibilitySkill(skill) {
  if (Number(skill.publishStatus) === 0) {
    message.warning('先上线，再设置公开范围')
    return
  }
  if (Number(skill.publishStatus) === 2) {
    await publishPrivateSkill(skill)
    return
  }
  await publishSkill(skill)
}
async function onResourceFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    resourceSelectedFile.value = null
    return
  }
  if (!file.name.toLowerCase().endsWith('.md')) {
    message.warning('Resource 当前只支持 Markdown (.md) 文件')
    event.target.value = ''
    resourceSelectedFile.value = null
    return
  }
  resourceSelectedFile.value = file
  model.value.fileName = file.name
  model.value.fileSize = file.size
  if (!model.value.name) {
    model.value.name = file.name.replace(/\.md$/i, '')
  }
  resourcePreviewText.value = await file.text()
}

async function loadResourcePreview(resourceId = model.value.id) {
  if (!resourceId) {
    resourcePreviewText.value = ''
    return
  }
  resourcePreviewLoading.value = true
  try {
    const presign = await api(`/api/share/studio/resources/${resourceId}/presign-download`)
    const response = await fetch(presign.downloadUrl)
    if (!response.ok) {
      throw new Error(`文档读取失败：${response.status}`)
    }
    resourcePreviewText.value = await response.text()
  } catch (error) {
    resourcePreviewText.value = error.message || '文档读取失败'
  } finally {
    resourcePreviewLoading.value = false
  }
}

async function uploadResourceFileIfNeeded() {
  if (!resourceSelectedFile.value) {
    return
  }
  if (!model.value.resourceUri) {
    throw new Error('请先填写 Resource URI')
  }
  const file = resourceSelectedFile.value
  const presign = await api('/api/share/studio/resources/presign-upload', {
    method: 'POST',
    body: {
      resourceUri: model.value.resourceUri,
      fileName: file.name,
      fileSize: file.size
    }
  })
  const uploadResponse = await fetch(presign.uploadUrl, {
    method: 'PUT',
    body: file
  })
  if (!uploadResponse.ok) {
    throw new Error(`Markdown 上传失败：${uploadResponse.status}`)
  }
  model.value.objectKey = presign.objectKey
  model.value.fileName = file.name
  model.value.fileSize = file.size
  resourceSelectedFile.value = null
}

async function saveResourceEditor() {
  resourceSaving.value = true
  try {
    await uploadResourceFileIfNeeded()
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/resources/${model.value.id}` : '/api/share/studio/resources'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    await loadResourcePreview(result.id)
    message.success(model.value.id ? 'Resource 已保存' : 'Resource 已创建')
    return result
  } catch (error) {
    message.error(error.message || '保存失败')
    throw error
  } finally {
    resourceSaving.value = false
  }
}

async function publishResource(resource) {
  const result = await api(`/api/share/studio/resources/${resource.id}/publish`, { method: 'POST' })
  if (model.value.id === resource.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Resource 已公开发布')
  await load()
}

async function publishPrivateResource(resource) {
  const result = await api(`/api/share/studio/resources/${resource.id}/publish-private`, { method: 'POST' })
  if (model.value.id === resource.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Resource 已设为不公开')
  await load()
}

async function unpublishResource(resource) {
  const result = await api(`/api/share/studio/resources/${resource.id}/unpublish`, { method: 'POST' })
  if (model.value.id === resource.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Resource 已下线')
  await load()
}

async function toggleOnlineResource(resource) {
  if (Number(resource.publishStatus) !== 0) {
    await unpublishResource(resource)
    return
  }
  await publishPrivateResource(resource)
}

async function toggleVisibilityResource(resource) {
  if (Number(resource.publishStatus) === 0) {
    message.warning('先上线，再设置公开范围')
    return
  }
  if (Number(resource.publishStatus) === 2) {
    await publishPrivateResource(resource)
    return
  }
  await publishResource(resource)
}
async function publishTool(tool) {
  const result = await api(`/api/share/studio/tools/${tool.id}/publish`, { method: 'POST' })
  if (model.value.id === tool.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Tool 已公开发布')
  await load()
}
async function publishPrivateTool(tool) {
  const result = await api(`/api/share/studio/tools/${tool.id}/publish-private`, { method: 'POST' })
  if (model.value.id === tool.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Tool 已设为不公开')
  await load()
}
async function unpublishTool(tool) {
  const result = await api(`/api/share/studio/tools/${tool.id}/unpublish`, { method: 'POST' })
  if (model.value.id === tool.id) {
    model.value = { ...model.value, ...result }
  }
  message.success('Tool 已下线')
  await load()
}
async function toggleOnlineTool(tool) {
  if (Number(tool.publishStatus) !== 0) {
    await unpublishTool(tool)
    return
  }

  await publishPrivateTool(tool)
}
async function toggleVisibilityTool(tool) {
  if (Number(tool.publishStatus) === 0) {
    message.warning('先上线，再设置公开范围')
    return
  }
  if (Number(tool.publishStatus) === 2) {
    await publishPrivateTool(tool)
    return
  }

  await publishTool(tool)
}
async function publishApi(apiConfig) {
  await api(`/api/share/studio/apis/${apiConfig.id}/publish`, { method: 'POST' })
  message.success('已公开发布')
  await load()
}
async function publishPrivateApi(apiConfig) {
  await api(`/api/share/studio/apis/${apiConfig.id}/publish-private`, { method: 'POST' })
  message.success('已设为不公开')
  await load()
}
async function unpublishApi(apiConfig) {
  await api(`/api/share/studio/apis/${apiConfig.id}/unpublish`, { method: 'POST' })
  message.success('已下架为草稿')
  await load()
}
async function toggleOnlineApi(apiConfig) {
  if (Number(apiConfig.publishStatus) === 0) {
    await publishPrivateApi(apiConfig)
    return
  }

  await unpublishApi(apiConfig)
}
async function toggleVisibilityApi(apiConfig) {
  if (Number(apiConfig.publishStatus) === 0) {
    message.warning('先上线，再设置公开范围')
    return
  }
  if (Number(apiConfig.publishStatus) === 2) {
    await publishPrivateApi(apiConfig)
    return
  }

  await publishApi(apiConfig)
}
function showLogin() {
  loggedIn.value = false
  rows.value = []
  drawer.value = false
  userRoleOpen.value = false
  toolPermissionOpen.value = false
  promptPermissionOpen.value = false
  resourcePermissionOpen.value = false
  tokenSelectionOpen.value = false
  tokenPromptSelectionOpen.value = false
  tokenResourceSelectionOpen.value = false
  dynamicToolDetailOpen.value = false
  auditDetailOpen.value = false
  apiDebugOpen.value = false
  rawToken.value = ''
}

onMounted(() => {
  window.addEventListener(UNAUTHORIZED_EVENT, showLogin)
  window.addEventListener('popstate', syncPageFromLocation)

  if (loggedIn.value) {
    load()
  }
})

onUnmounted(() => {
  window.removeEventListener(UNAUTHORIZED_EVENT, showLogin)
  window.removeEventListener('popstate', syncPageFromLocation)
})

watch(selectedToolRequestKeys, () => {
  tryAutoGenerateToolAssets()
  generateToolDebugParamsFromSelection()
})

watch(selectedToolDataSourceIds, () => {
  tryAutoGenerateToolAssets()
})

watch(selectedPromptToolNames, () => {
  syncPromptToolsIntoTemplate()
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
  <template v-if="isSharePage">
    <div class="share-shell">
      <nav class="hub-nav">
        <div class="hub-nav-inner">
          <div v-if="isCommunityPage" class="hub-nav-tabs">
            <a class="hub-logo" @click.prevent="changePage('shareHome')"><AppstoreOutlined />Bear 社区</a>
            <a :class="['hub-nav-tab', page === 'shareHome' ? 'active' : '']" @click.prevent="changePage('shareHome')">首页</a>
            <a class="hub-nav-tab">Skills 社区</a>
            <a :class="['hub-nav-tab', page === 'shareTools' ? 'active' : '']" @click.prevent="changePage('shareTools')">MCP Tools</a>
            <a class="hub-nav-tab">MCP Prompts</a>
            <a :class="['hub-nav-tab', page === 'shareApis' ? 'active' : '']" @click.prevent="changePage('shareApis')">API 能力</a>
            <a class="hub-nav-tab">我的MCP配置</a>
            <a class="hub-nav-tab">Token 管理</a>
          </div>
          <div v-else class="hub-nav-tabs">
            <a class="hub-logo" @click.prevent="changePage('studioHome')"><RocketOutlined />Bear 创作空间</a>
            <a :class="['hub-nav-tab', page === 'studioHome' ? 'active' : '']" @click.prevent="changePage('studioHome')">首页</a>
            <a :class="['hub-nav-tab', ['studioSkills','studioSkillEdit'].includes(page) ? 'active' : '']" @click.prevent="changePage('studioSkills')">Skills 创作</a>
            <a :class="['hub-nav-tab', ['studioTools','studioToolEdit'].includes(page) ? 'active' : '']" @click.prevent="changePage('studioTools')">Tools 创作</a>
            <a :class="['hub-nav-tab', ['studioPrompts','studioPromptEdit'].includes(page) ? 'active' : '']" @click.prevent="changePage('studioPrompts')">Prompts 创作</a>
            <a :class="['hub-nav-tab', ['studioResources','studioResourceEdit'].includes(page) ? 'active' : '']" @click.prevent="changePage('studioResources')">Resources 创作</a>
            <a :class="['hub-nav-tab', ['studioApis','studioApiEdit'].includes(page) ? 'active' : '']" @click.prevent="changePage('studioApis')">API 创作</a>
          </div>
          <div class="hub-nav-right">
            <span class="hub-nav-user">demo-admin</span>
            <a v-if="isCommunityPage" class="hub-nav-link" @click.prevent="changePage('studioHome')"><RocketOutlined />创作空间</a>
            <a v-else class="hub-nav-link" @click.prevent="changePage('shareHome')"><HomeOutlined />返回社区</a>
            <a class="hub-nav-link" @click.prevent="changePage('dashboard')"><SettingOutlined />管理员</a>
            <a class="hub-nav-link"><LogoutOutlined />退出</a>
          </div>
        </div>
      </nav>

      <section class="hub-hero">
        <h1>{{ title }}</h1>
        <p>{{ desc }}</p>
      </section>

      <main class="hub-main">
        <template v-if="page === 'shareHome'">
          <section class="community-market-shell">
            <div class="community-search-row">
              <div class="community-search">
                <SearchOutlined />
                <input v-model="communityKeyword" type="text" placeholder="搜索工具名、API Key、描述..." />
              </div>
              <button type="button" class="community-create-btn" @click="changePage('studioHome')"><PlusOutlined />创作能力</button>
            </div>

            <div class="community-overview">
              <button type="button" class="community-stat-card" @click="changePage('shareTools')">
                <span><ThunderboltOutlined /></span>
                <b>{{ communityTools.length + communityBuiltinTools.length }}</b>
                <small>公开 MCP Tools</small>
              </button>
              <button type="button" class="community-stat-card" @click="changePage('shareApis')">
                <span><KeyOutlined /></span>
                <b>{{ communityApis.length }}</b>
                <small>公开 API 能力</small>
              </button>
              <div class="community-rule-card">
                <b>公开负责发现，调用仍走权限</b>
                <p>社区展示只说明能力存在，真正进入 tools/list / tools/call 还需要角色资格和 Token 工具选择。</p>
              </div>
            </div>

            <div class="community-section-head">
              <div>
                <h2>公开 MCP Tools</h2>
                <p>AI Agent 最终看到和调用的是这些 Tool。</p>
              </div>
              <button type="button" @click="changePage('shareTools')">查看全部</button>
            </div>

            <div v-if="filteredCommunityTools.length" class="community-card-grid">
              <article v-for="item in filteredCommunityTools.slice(0, 6)" :key="`${item.communityType}-${item.id}`" class="community-card tool" @click="openCommunityToolDetail(item)">
                <div class="community-card-top">
                  <div class="community-kind">
                    <span class="community-card-icon"><ThunderboltOutlined /></span>
                    <b>{{ item.communityType === 'builtin' ? '内置工具' : '动态工具' }}</b>
                  </div>
                  <em>{{ communityToolCode(item) }}</em>
                </div>
                <h3>{{ item.displayName || '未命名 Tool' }}</h3>
                <p>{{ item.displayDescription || '暂无描述' }}</p>
                <div class="community-tags">
                  <span>{{ communityToolCountLabel(item) }}</span>
                  <span v-for="key in parseJsonArray(item.linkedRequestKeys).slice(0, 2)" :key="key">{{ key }}</span>
                  <span v-if="parseJsonArray(item.linkedRequestKeys).length > 2">+{{ parseJsonArray(item.linkedRequestKeys).length - 2 }}</span>
                </div>
                <div class="community-card-foot">
                  <span>管理员</span>
                  <span>♡ 0</span>
                  <button type="button" @click.stop="openCommunityToolDetail(item)">查看</button>
                </div>
              </article>
            </div>
            <div v-else class="community-empty">还没有公开的 MCP Tool。</div>

            <div class="community-section-head">
              <div>
                <h2>公开 API 能力</h2>
                <p>API 是 Tool 编排时可复用的底层接入能力。</p>
              </div>
              <button type="button" @click="changePage('shareApis')">查看全部</button>
            </div>

            <div v-if="filteredCommunityApis.length" class="community-card-grid api">
              <article v-for="item in filteredCommunityApis.slice(0, 6)" :key="item.id" class="community-card api" @click="openCommunityApiDetail(item)">
                <div class="community-card-top">
                  <div class="community-kind">
                    <span class="community-card-icon"><KeyOutlined /></span>
                    <b>API 能力</b>
                  </div>
                  <em>{{ communityCode('API', item.id) }}</em>
                </div>
                <h3>{{ item.configKey || item.name || '未命名 API' }}</h3>
                <p>{{ item.description || item.name || '暂无描述' }}</p>
                <div class="community-tags">
                  <span>{{ item.method || 'GET' }}</span>
                  <span>{{ item.category || '未分类' }}</span>
                </div>
                <div class="community-card-foot">
                  <span>管理员</span>
                  <span>♡ 0</span>
                  <button type="button" @click.stop="openCommunityApiDetail(item)">查看</button>
                </div>
              </article>
            </div>
            <div v-else class="community-empty">还没有公开的 API。</div>
          </section>
        </template>

        <template v-else-if="page === 'shareTools'">
          <section class="community-market-shell">
            <div class="community-search-row">
              <div class="community-search">
                <SearchOutlined />
                <input v-model="communityKeyword" type="text" placeholder="搜索 Tool 名称、描述、API 白名单..." />
              </div>
              <span class="community-count">共 {{ filteredCommunityTools.length }} 个 Tool</span>
            </div>
            <div class="community-type-switch">
              <button type="button" :class="{active: communityToolTypeFilter === 'all'}" @click="communityToolTypeFilter = 'all'">全部</button>
              <button type="button" :class="{active: communityToolTypeFilter === 'dynamic'}" @click="communityToolTypeFilter = 'dynamic'">动态工具</button>
              <button type="button" :class="{active: communityToolTypeFilter === 'builtin'}" @click="communityToolTypeFilter = 'builtin'">内置工具</button>
            </div>

            <div v-if="filteredCommunityTools.length" class="community-card-grid">
              <article v-for="item in filteredCommunityTools" :key="`${item.communityType}-${item.id}`" class="community-card tool" @click="openCommunityToolDetail(item)">
                <div class="community-card-top">
                  <div class="community-kind">
                    <span class="community-card-icon"><ThunderboltOutlined /></span>
                    <b>{{ item.communityType === 'builtin' ? '内置工具' : '动态工具' }}</b>
                  </div>
                  <em>{{ communityToolCode(item) }}</em>
                </div>
                <h3>{{ item.displayName || '未命名 Tool' }}</h3>
                <p>{{ item.displayDescription || '暂无描述' }}</p>
                <div class="community-tags">
                  <span>{{ communityToolCountLabel(item) }}</span>
                  <span v-for="key in parseJsonArray(item.linkedRequestKeys).slice(0, 3)" :key="key">{{ key }}</span>
                </div>
                <div class="community-card-foot">
                  <span>管理员</span>
                  <span>♡ 0</span>
                  <button type="button" @click.stop="openCommunityToolDetail(item)">查看</button>
                </div>
              </article>
            </div>
            <div v-else class="community-empty">没有匹配的公开 MCP Tool。</div>
          </section>
        </template>

        <template v-else-if="page === 'shareApis'">
          <section class="community-market-shell">
            <div class="community-search-row">
              <div class="community-search">
                <SearchOutlined />
                <input v-model="communityKeyword" type="text" placeholder="搜索 API Key、名称、分类或描述..." />
              </div>
              <span class="community-count">共 {{ filteredCommunityApis.length }} 个 API</span>
            </div>

            <div v-if="filteredCommunityApis.length" class="community-card-grid api">
              <article v-for="item in filteredCommunityApis" :key="item.id" class="community-card api" @click="openCommunityApiDetail(item)">
                <div class="community-card-top">
                  <div class="community-kind">
                    <span class="community-card-icon"><KeyOutlined /></span>
                    <b>API 能力</b>
                  </div>
                  <em>{{ communityCode('API', item.id) }}</em>
                </div>
                <h3>{{ item.configKey || item.name || '未命名 API' }}</h3>
                <p>{{ item.description || item.name || '暂无描述' }}</p>
                <div class="community-tags">
                  <span>{{ item.type || 'HTTP' }}</span>
                  <span>{{ item.method || 'GET' }}</span>
                  <span>{{ item.category || '未分类' }}</span>
                </div>
                <div class="community-card-foot">
                  <span>管理员</span>
                  <span>♡ 0</span>
                  <button type="button" @click.stop="openCommunityApiDetail(item)">查看</button>
                </div>
              </article>
            </div>
            <div v-else class="community-empty">没有匹配的公开 API。</div>
          </section>
        </template>

        <template v-else-if="page === 'studioHome'">
          <section class="studio-workbench">
            <div class="studio-command">
              <span class="studio-kicker">Creator Hub</span>
              <h2>把经验、接口和脚本沉淀成团队可复用的 AI 能力</h2>
              <p>这里是能力作者的工作台：可以写 Skill、编排 Tool、沉淀 Prompt，也可以接入 API。完成后再发布到社区或进入 MCP 调用链路。</p>
              <div class="studio-command-actions">
                <button type="button" class="studio-primary-action"><PlusOutlined />开始创作</button>
                <button type="button" class="studio-secondary-action" @click="changePage('shareHome')"><HomeOutlined />浏览社区</button>
              </div>
            </div>

            <div class="studio-status-panel">
              <div class="studio-panel-head">
                <span>创作概览</span>
              <b>Creator</b>
              </div>
              <div class="studio-panel-metrics">
                <div><b>4</b><span>创作类型</span></div>
                <div><b>{{ rows.length || 0 }}</b><span>已接 API</span></div>
                <div><b>1</b><span>发布入口</span></div>
              </div>
              <div class="studio-path">
                <span class="done">选择能力类型</span>
                <span class="current">完善能力内容</span>
                <span>发布到社区</span>
              </div>
            </div>
          </section>

          <section class="studio-lanes">
            <a class="studio-lane skill" @click.prevent="changePage('studioSkills')">
              <span class="studio-lane-icon"><DatabaseOutlined /></span>
              <small>01</small>
              <h3>Skills 创作</h3>
              <p>把规范、步骤和上下文沉淀为 AI 可执行的工作说明。</p>
            </a>
            <a class="studio-lane tool" @click.prevent="changePage('studioTools')">
              <span class="studio-lane-icon"><ThunderboltOutlined /></span>
              <small>02</small>
              <h3>Tools 创作</h3>
              <p>把脚本逻辑和企业能力编排成可调用的 MCP 工具。</p>
            </a>
            <a class="studio-lane prompt" @click.prevent="changePage('studioPrompts')">
              <span class="studio-lane-icon"><AuditOutlined /></span>
              <small>03</small>
              <h3>Prompts 创作</h3>
              <p>维护结构化提示词模板，统一团队提问和输出格式。</p>
            </a>
            <a class="studio-lane skill" @click.prevent="changePage('studioResources')">
              <span class="studio-lane-icon"><DatabaseOutlined /></span>
              <small>04</small>
              <h3>Resources 创作</h3>
              <p>上传 Markdown 知识资源，让 Agent 按 URI 读取上下文。</p>
            </a>
            <a class="studio-lane api" @click.prevent="changePage('studioApis')">
              <span class="studio-lane-icon"><KeyOutlined /></span>
              <small>05</small>
              <h3>API 创作</h3>
              <p>接入外部 HTTP API，作为后续工具编排的基础能力。</p>
            </a>
          </section>
        </template>

        <template v-else-if="page === 'studioSkills'">
          <section class="api-library-shell tool-library-shell">
            <div class="api-library-head">
              <span class="api-section-kicker">Skill Workspace</span>
              <h2>Skills 创作</h2>
              <p>上传单文件 SKILL.md，发布后 Agent 可以通过 get_skill 获取并安装到 Cursor。</p>
              <div class="api-library-search">
                <SearchOutlined />
                <input v-model="skillKeyword" type="text" placeholder="搜索 Skill ID、名称、描述、分类或文件名..." />
              </div>
            </div>

            <div class="api-library-toolbar">
              <div class="api-status-switch">
                <button type="button" :class="{active: skillStatusFilter === 'all'}" @click="skillStatusFilter = 'all'">全部</button>
                <button type="button" :class="{active: skillStatusFilter === 'online'}" @click="skillStatusFilter = 'online'">已上线</button>
                <button type="button" :class="{active: skillStatusFilter === 'draft'}" @click="skillStatusFilter = 'draft'">草稿</button>
              </div>
              <div class="api-library-side">
                <span>共 {{ filteredStudioSkills.length }} 个 Skill</span>
                <button type="button" class="api-library-new" @click="openCreate"><PlusOutlined />新建</button>
              </div>
            </div>
            <div class="api-visibility-note">
              <span><i class="public"></i>公开：进入社区展示</span>
              <span><i class="private"></i>不公开：可通过 get_skill 安装但不进入公开展示</span>
            </div>

            <div v-if="filteredStudioSkills.length" class="api-library-grid">
              <article v-for="item in filteredStudioSkills" :key="item.id" class="api-library-card tool-library-card">
                <div class="api-card-top">
                  <span class="api-card-icon"><DatabaseOutlined /></span>
                  <div class="api-card-badges">
                    <span :class="['api-card-status', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                    <span v-if="Number(item.publishStatus) !== 0" :class="['api-card-visibility', visibilityClass(item.publishStatus)]">{{ visibilityLabel(item.publishStatus) }}</span>
                  </div>
                </div>
                <div class="api-card-title-row">
                  <h3>{{ item.name || '未命名 Skill' }}</h3>
                  <code>{{ item.skillCode || '-' }}</code>
                </div>
                <p>{{ item.description || '还没有填写描述，建议说明这个 Skill 适合什么任务场景。' }}</p>
                <div class="tool-card-meta">
                  <span>Cursor Skill</span>
                  <span>{{ item.category || '未分类' }}</span>
                  <span>{{ item.fileSize ? `${Math.ceil(item.fileSize / 1024)} KB` : '未记录大小' }}</span>
                </div>
                <div class="tool-card-whitelist">
                  <span>{{ item.fileName || '未上传文件' }}</span>
                  <span>{{ item.skillCode || '保存时生成 Skill ID' }}</span>
                </div>
                <div class="api-card-foot">
                  <span class="tool-script-preview">{{ compactText(item.description || item.fileName, 34) }}</span>
                  <div class="tool-card-actions">
                    <button type="button" @click="openSkillEditor(item)">编辑</button>
                    <button type="button" :disabled="Number(item.publishStatus) === 0" @click="openCursorSkillInstall(item)">Cursor 安装</button>
                    <button type="button" :disabled="Number(item.publishStatus) === 0" @click="downloadSkillZip(item)">下载 ZIP</button>
                    <button type="button" :class="Number(item.publishStatus) === 0 ? 'publish' : 'danger'" @click="toggleOnlineSkill(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                    <button type="button" class="private" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilitySkill(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                  </div>
                </div>
              </article>
            </div>

            <div v-else class="api-empty-panel">
              <div class="api-empty-mark"><DatabaseOutlined /></div>
              <h3>{{ rows.length ? '没有匹配的 Skill' : '还没有创建 Skill' }}</h3>
              <p>{{ rows.length ? '换个关键词或筛选条件再看看。' : '先上传一个 SKILL.md，保存后通过 get_skill 分发给 Cursor Agent 安装。' }}</p>
              <div class="api-empty-steps">
                <span>上传 SKILL.md</span>
                <span>发布上线</span>
                <span>get_skill 安装</span>
              </div>
              <button v-if="!rows.length" type="button" @click="openCreate"><PlusOutlined />新建 Skill</button>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioTools'">
          <section class="api-library-shell tool-library-shell">
            <div class="api-library-head">
              <span class="api-section-kicker">Tool Workspace</span>
              <h2>Tools 创作</h2>
              <p>把已接入的 API 配置包装成 MCP Tool，让 AI Agent 能在 tools/list 中看到并通过 tools/call 调用。</p>
              <div class="api-library-search">
                <SearchOutlined />
                <input v-model="toolKeyword" type="text" placeholder="搜索工具名、描述、API 白名单或脚本..." />
              </div>
            </div>

            <div class="api-library-toolbar">
              <div class="api-status-switch">
                <button type="button" :class="{active: toolStatusFilter === 'all'}" @click="toolStatusFilter = 'all'">全部</button>
                <button type="button" :class="{active: toolStatusFilter === 'online'}" @click="toolStatusFilter = 'online'">已上线</button>
                <button type="button" :class="{active: toolStatusFilter === 'draft'}" @click="toolStatusFilter = 'draft'">草稿</button>
              </div>
              <div class="api-library-side">
                <span>共 {{ filteredStudioTools.length }} 个 Tool</span>
                <button type="button" class="api-library-new" @click="openCreate"><PlusOutlined />新建</button>
              </div>
            </div>
            <div class="api-visibility-note">
              <span><i class="public"></i>公开：进入社区</span>
              <span><i class="private"></i>不公开：已上线但只在自己的创作空间可见</span>
            </div>

            <div v-if="filteredStudioTools.length" class="api-library-grid">
              <article v-for="item in filteredStudioTools" :key="item.id" class="api-library-card tool-library-card">
                <div class="api-card-top">
                  <span class="api-card-icon"><ThunderboltOutlined /></span>
                  <div class="api-card-badges">
                    <span :class="['api-card-status', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                    <span v-if="Number(item.publishStatus) !== 0" :class="['api-card-visibility', visibilityClass(item.publishStatus)]">{{ visibilityLabel(item.publishStatus) }}</span>
                  </div>
                </div>
                <div class="api-card-title-row">
                  <h3>{{ item.toolName || '未命名 Tool' }}</h3>
                </div>
                <p>{{ item.toolDescription || '还没有填写工具描述，建议说明这个工具能帮 Agent 完成什么任务。' }}</p>
                <div class="tool-card-meta">
                  <span>MCP Tool</span>
                  <span>{{ parseJsonArray(item.linkedRequestKeys).length }} 个 API</span>
                  <span>{{ parseJsonArray(item.linkedDataSourceIds).length }} 个数据源</span>
                </div>
                <div class="tool-card-whitelist">
                  <span v-for="key in parseJsonArray(item.linkedRequestKeys)" :key="key">{{ key }}</span>
                  <span v-for="id in parseJsonArray(item.linkedDataSourceIds)" :key="`ds-${id}`">DS {{ id }}</span>
                  <span v-if="!parseJsonArray(item.linkedRequestKeys).length">未绑定 API</span>
                  <span v-if="!parseJsonArray(item.linkedDataSourceIds).length">未绑定数据源</span>
                </div>
                <div class="api-card-foot">
                  <span class="tool-script-preview">{{ compactText(item.groovyScript, 34) }}</span>
                  <div class="tool-card-actions">
                    <button type="button" @click="openToolEditor(item)">编辑</button>
                    <button type="button" :class="Number(item.publishStatus) === 0 ? 'publish' : 'danger'" @click="toggleOnlineTool(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                    <button type="button" class="private" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityTool(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                  </div>
                </div>
              </article>
            </div>

            <div v-else class="api-empty-panel">
              <div class="api-empty-mark"><ThunderboltOutlined /></div>
              <h3>{{ rows.length ? '没有匹配的 Tool' : '还没有创建 Tool' }}</h3>
              <p>{{ rows.length ? '换个关键词或筛选条件再看看。' : '先选择已上线的 API 配置，再编写 Groovy 脚本，把它包装成 MCP Tool。' }}</p>
              <div class="api-empty-steps">
                <span>选择 API</span>
                <span>编写脚本</span>
                <span>调试发布</span>
              </div>
              <button v-if="!rows.length" type="button" @click="openCreate"><PlusOutlined />新建 Tool</button>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioPrompts'">
          <section class="api-library-shell tool-library-shell">
            <div class="api-library-head">
              <span class="api-section-kicker">Prompt Workspace</span>
              <h2>Prompts 创作</h2>
              <p>沉淀企业工作流模板，告诉 Agent 应该按什么流程、使用哪些工具、输出什么结构。</p>
              <div class="api-library-search">
                <SearchOutlined />
                <input v-model="promptKeyword" type="text" placeholder="搜索 Prompt 名称、标题、描述、模板或工具..." />
              </div>
            </div>

            <div class="api-library-toolbar">
              <div class="api-status-switch">
                <button type="button" :class="{active: promptStatusFilter === 'all'}" @click="promptStatusFilter = 'all'">全部</button>
                <button type="button" :class="{active: promptStatusFilter === 'online'}" @click="promptStatusFilter = 'online'">已上线</button>
                <button type="button" :class="{active: promptStatusFilter === 'draft'}" @click="promptStatusFilter = 'draft'">草稿</button>
              </div>
              <div class="api-library-side">
                <span>共 {{ filteredStudioPrompts.length }} 个 Prompt</span>
                <button type="button" class="api-library-new" @click="openCreate"><PlusOutlined />新建</button>
              </div>
            </div>
            <div class="api-visibility-note">
              <span><i class="public"></i>公开：进入社区</span>
              <span><i class="private"></i>不公开：已上线但只在自己的创作空间可见</span>
            </div>

            <div v-if="filteredStudioPrompts.length" class="api-library-grid">
              <article v-for="item in filteredStudioPrompts" :key="item.id" class="api-library-card tool-library-card">
                <div class="api-card-top">
                  <span class="api-card-icon"><AuditOutlined /></span>
                  <div class="api-card-badges">
                    <span :class="['api-card-status', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                    <span v-if="Number(item.publishStatus) !== 0" :class="['api-card-visibility', visibilityClass(item.publishStatus)]">{{ visibilityLabel(item.publishStatus) }}</span>
                  </div>
                </div>
                <div class="api-card-title-row">
                  <h3>{{ item.title || '未命名 Prompt' }}</h3>
                  <code>{{ item.promptName || '-' }}</code>
                </div>
                <p>{{ item.description || '还没有填写描述，建议说明这个模板适合什么工作流。' }}</p>
                <div class="tool-card-meta">
                  <span>MCP Prompt</span>
                  <span>{{ parseJsonArray(item.argumentsSchema).length }} 个参数</span>
                  <span>{{ parseJsonArray(item.linkedToolNames).length }} 个建议工具</span>
                </div>
                <div class="tool-card-whitelist">
                  <span v-for="name in parseJsonArray(item.linkedToolNames)" :key="name">{{ name }}</span>
                  <span v-if="!parseJsonArray(item.linkedToolNames).length">未选择工具</span>
                </div>
                <div class="api-card-foot">
                  <span class="tool-script-preview">{{ compactText(item.templateContent, 34) }}</span>
                  <div class="tool-card-actions">
                    <button type="button" @click="openPromptEditor(item)">编辑</button>
                    <button type="button" :class="Number(item.publishStatus) === 0 ? 'publish' : 'danger'" @click="toggleOnlinePrompt(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                    <button type="button" class="private" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityPrompt(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                  </div>
                </div>
              </article>
            </div>

            <div v-else class="api-empty-panel">
              <div class="api-empty-mark"><AuditOutlined /></div>
              <h3>{{ rows.length ? '没有匹配的 Prompt' : '还没有创建 Prompt' }}</h3>
              <p>{{ rows.length ? '换个关键词或筛选条件再看看。' : '先创建一个工作流模板，把工具使用顺序、参数要求和输出格式沉淀下来。' }}</p>
              <div class="api-empty-steps">
                <span>定义参数</span>
                <span>选择工具</span>
                <span>预览发布</span>
              </div>
              <button v-if="!rows.length" type="button" @click="openCreate"><PlusOutlined />新建 Prompt</button>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioResources'">
          <section class="api-library-shell tool-library-shell">
            <div class="api-library-head">
              <span class="api-section-kicker">Resource Workspace</span>
              <h2>Resources 创作</h2>
              <p>上传 Markdown 资源，发布后 Agent 可以通过 resources/list 发现，并用 resources/read 读取内容。</p>
              <div class="api-library-search">
                <SearchOutlined />
                <input v-model="resourceKeyword" type="text" placeholder="搜索 Resource URI、名称、描述或文件名..." />
              </div>
            </div>

            <div class="api-library-toolbar">
              <div class="api-status-switch">
                <button type="button" :class="{active: resourceStatusFilter === 'all'}" @click="resourceStatusFilter = 'all'">全部</button>
                <button type="button" :class="{active: resourceStatusFilter === 'online'}" @click="resourceStatusFilter = 'online'">已上线</button>
                <button type="button" :class="{active: resourceStatusFilter === 'draft'}" @click="resourceStatusFilter = 'draft'">草稿</button>
              </div>
              <div class="api-library-side">
                <span>共 {{ filteredStudioResources.length }} 个 Resource</span>
                <button type="button" class="api-library-new" @click="openCreate"><PlusOutlined />新建</button>
              </div>
            </div>
            <div class="api-visibility-note">
              <span><i class="public"></i>公开：进入社区</span>
              <span><i class="private"></i>不公开：已上线但只在权限链路中可选</span>
            </div>

            <div v-if="filteredStudioResources.length" class="api-library-grid">
              <article v-for="item in filteredStudioResources" :key="item.id" class="api-library-card tool-library-card">
                <div class="api-card-top">
                  <span class="api-card-icon"><DatabaseOutlined /></span>
                  <div class="api-card-badges">
                    <span :class="['api-card-status', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                    <span v-if="Number(item.publishStatus) !== 0" :class="['api-card-visibility', visibilityClass(item.publishStatus)]">{{ visibilityLabel(item.publishStatus) }}</span>
                  </div>
                </div>
                <div class="api-card-title-row">
                  <h3>{{ item.name || '未命名 Resource' }}</h3>
                  <code>{{ item.resourceUri || '-' }}</code>
                </div>
                <p>{{ item.description || '还没有填写描述，建议说明这份 Markdown 资源适合什么场景读取。' }}</p>
                <div class="tool-card-meta">
                  <span>MCP Resource</span>
                  <span>{{ item.mimeType || 'text/markdown' }}</span>
                  <span>{{ item.fileSize ? `${Math.ceil(item.fileSize / 1024)} KB` : '未记录大小' }}</span>
                </div>
                <div class="tool-card-whitelist">
                  <span>{{ item.fileName || '未上传文件' }}</span>
                  <span>{{ item.resourceUri || '未配置 URI' }}</span>
                </div>
                <div class="api-card-foot">
                  <span class="tool-script-preview">{{ compactText(item.description || item.fileName, 34) }}</span>
                  <div class="tool-card-actions">
                    <button type="button" @click="openResourceEditor(item)">编辑</button>
                    <button type="button" :class="Number(item.publishStatus) === 0 ? 'publish' : 'danger'" @click="toggleOnlineResource(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                    <button type="button" class="private" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityResource(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                  </div>
                </div>
              </article>
            </div>

            <div v-else class="api-empty-panel">
              <div class="api-empty-mark"><DatabaseOutlined /></div>
              <h3>{{ rows.length ? '没有匹配的 Resource' : '还没有创建 Resource' }}</h3>
              <p>{{ rows.length ? '换个关键词或筛选条件再看看。' : '先上传一份 Markdown 文件，保存为 MCP Resource，再配置角色和 Token 可见范围。' }}</p>
              <div class="api-empty-steps">
                <span>填写 URI</span>
                <span>上传 Markdown</span>
                <span>授权读取</span>
              </div>
              <button v-if="!rows.length" type="button" @click="openCreate"><PlusOutlined />新建 Resource</button>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioApis'">
          <section class="api-library-shell">
            <div class="api-library-head">
              <span class="api-section-kicker">API Workspace</span>
              <h2>API 创作</h2>
              <p>创建外部 HTTP API 配置，供后续动态工具 runRequest 调用。</p>
              <div class="api-library-search">
                <SearchOutlined />
                <input v-model="apiKeyword" type="text" placeholder="搜索接口 ID、配置键、名称或 URL..." />
              </div>
            </div>

            <div class="api-library-toolbar">
              <div class="api-status-switch">
                <button type="button" :class="{active: apiStatusFilter === 'all'}" @click="apiStatusFilter = 'all'">全部</button>
                <button type="button" :class="{active: apiStatusFilter === 'online'}" @click="apiStatusFilter = 'online'">已上线</button>
                <button type="button" :class="{active: apiStatusFilter === 'draft'}" @click="apiStatusFilter = 'draft'">草稿</button>
              </div>
              <div class="api-library-side">
                <span>共 {{ filteredStudioApis.length }} 个 API</span>
                <button type="button" class="api-library-new" @click="openCreate"><PlusOutlined />新建</button>
              </div>
            </div>
            <div class="api-visibility-note">
              <span><i class="public"></i>公开：进入社区</span>
              <span><i class="private"></i>不公开：只在自己的创作空间可见</span>
            </div>

            <div v-if="filteredStudioApis.length" class="api-library-grid">
              <article v-for="item in filteredStudioApis" :key="item.id" class="api-library-card">
                <div class="api-card-top">
                  <span class="api-card-icon"><KeyOutlined /></span>
                  <div class="api-card-badges">
                    <span :class="['api-card-status', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                    <span v-if="Number(item.publishStatus) !== 0" :class="['api-card-visibility', visibilityClass(item.publishStatus)]">{{ visibilityLabel(item.publishStatus) }}</span>
                  </div>
                </div>
                <div class="api-card-title-row">
                  <h3>{{ item.configKey || item.name || '未命名 API' }}</h3>
                  <code>{{ item.requestId || '-' }}</code>
                </div>
                <p>{{ item.description || item.name || '还没有填写描述，建议说明这个接口适合被哪个动态工具调用。' }}</p>
                <div class="api-card-route">
                  <span>{{ item.type || 'HTTP' }}</span>
                  <b>{{ item.method || 'GET' }}</b>
                  <code>{{ item.url || '尚未配置请求 URL' }}</code>
                </div>
                <div class="api-card-foot">
                  <span>{{ item.category || '未分类' }}</span>
                  <div>
                    <button type="button" @click="openApiDebug(item)">调试</button>
                    <button type="button" @click="openApiEditor(item)">编辑</button>
                    <button type="button" :class="Number(item.publishStatus) === 0 ? 'publish' : 'danger'" @click="toggleOnlineApi(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                    <button type="button" class="private" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityApi(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                  </div>
                </div>
              </article>
            </div>

            <div v-else class="api-empty-panel">
              <div class="api-empty-mark"><KeyOutlined /></div>
              <h3>{{ rows.length ? '没有匹配的 API' : '还没有接入 API' }}</h3>
              <p>{{ rows.length ? '换个关键词或筛选条件再看看。' : '先创建一个 HTTP API，保存真实接口配置，再用发送调试验证响应。' }}</p>
              <div class="api-empty-steps">
                <span>配置接口</span>
                <span>发送调试</span>
                <span>发布能力</span>
              </div>
              <button v-if="!rows.length" type="button" @click="openCreate"><PlusOutlined />新建 HTTP API</button>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioPromptEdit'">
          <section class="tool-editor-shell">
            <div class="api-editor-header">
              <div>
                <button type="button" class="api-back-btn" @click="changePage('studioPrompts')">← 返回列表</button>
                <span class="api-section-kicker">MCP Prompt</span>
                <h2>{{ model.id ? '编辑 Prompt' : '新建 Prompt' }}</h2>
                <p>Prompt 模板负责沉淀工作流：写清楚 Agent 应该使用哪些工具、按什么顺序执行，以及最终如何组织答案。</p>
              </div>
              <aside>
                <span>当前模板</span>
                <b>{{ model.promptName || '未命名 Prompt' }}</b>
                <small>{{ Number(model.publishStatus) === 0 ? '草稿状态，可先预览再上线' : `${publishLabel(model.publishStatus)}，${visibilityLabel(model.publishStatus)}` }}</small>
              </aside>
            </div>

            <div class="tool-editor-board">
              <aside class="tool-editor-meta">
                <div class="tool-editor-panel-head">
                  <b>基本信息</b>
                  <span>Prompt 名称会出现在 prompts/list</span>
                </div>
                <label>
                  <span>Prompt 名称</span>
                  <input v-model="model.promptName" placeholder="如 analyze_user_growth" />
                </label>
                <label>
                  <span>标题</span>
                  <input v-model="model.title" placeholder="如 用户增长分析流程" />
                </label>
                <label>
                  <span>描述</span>
                  <textarea v-model="model.description" spellcheck="false" placeholder="说明这个模板适合什么任务"></textarea>
                </label>

                <div class="tool-editor-panel-head compact">
                  <b>参数</b>
                  <button type="button" @click="addPromptArgument"><PlusOutlined />新增</button>
                </div>
                <p class="tool-api-empty">模板里的 <code v-pre>{{topic}}</code> 对应参数名 topic，可从模板自动提取。</p>
                <div v-if="promptArguments().length" class="prompt-param-list">
                  <div v-for="(arg, index) in promptArguments()" :key="arg._key" class="prompt-param-item">
                    <input :value="arg.name" placeholder="参数名" @input="updatePromptArgument(index, 'name', $event.target.value)" />
                    <input :value="arg.description" placeholder="描述" @input="updatePromptArgument(index, 'description', $event.target.value)" />
                    <input :value="arg.defaultValue" placeholder="默认值" @input="updatePromptArgument(index, 'defaultValue', $event.target.value)" />
                    <label class="prompt-param-check">
                      <input type="checkbox" :checked="Boolean(arg.required)" @change="updatePromptArgument(index, 'required', $event.target.checked)" />
                      <span>必填</span>
                    </label>
                    <button type="button" @click="removePromptArgument(index)">删除</button>
                  </div>
                </div>
                <p v-else class="tool-api-empty">暂无参数，可以手动新增，或从模板内容自动提取。</p>

                <div class="tool-editor-panel-head compact">
                  <b>建议工具</b>
                  <span>已选 {{ selectedPromptToolNames.length }} 个</span>
                </div>
                <a-select
                  v-model:value="selectedPromptToolNames"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="promptToolOptions"
                  option-filter-prop="label"
                  placeholder="选择这个流程建议使用的 Tool"
                  class="tool-api-select"
                  popupClassName="dark-select-dropdown tool-api-dropdown"
                  max-tag-count="responsive"
                />
              </aside>

              <section class="tool-script-editor">
                <div class="tool-editor-panel-head">
                  <b>模板内容</b>
                  <span class="tool-head-actions">
                    <button type="button" @click="extractPromptArgumentsFromTemplate">提取参数</button>
                    <button type="button" @click="insertPromptToolsPlaceholder">插入工具清单</button>
                  </span>
                </div>
                <textarea v-model="model.templateContent" spellcheck="false"></textarea>
                <div class="tool-editor-actions">
                  <button type="button" class="tool-save-btn" :disabled="promptSaving" @click="savePromptEditor">{{ promptSaving ? '保存中' : '保存 Prompt' }}</button>
                  <button type="button" class="tool-run-btn" :disabled="promptPreviewLoading" @click="previewPromptEditor">{{ promptPreviewLoading ? '预览中' : '预览渲染' }}</button>
                  <button v-if="model.id" type="button" class="tool-online-btn" @click="toggleOnlinePrompt(model)">
                    {{ onlineActionLabel(model.publishStatus) }}
                  </button>
                  <button v-if="model.id" type="button" class="tool-online-btn" :disabled="Number(model.publishStatus) === 0" @click="toggleVisibilityPrompt(model)">
                    {{ visibilityActionLabel(model.publishStatus) }}
                  </button>
                </div>
              </section>

              <aside class="tool-debug-panel">
                <div class="tool-editor-panel-head">
                  <b>预览参数</b>
                  <button type="button" @click="promptPreviewArgs = buildPromptPreviewArgs()">重置</button>
                </div>
                <textarea v-model="promptPreviewArgs" spellcheck="false"></textarea>
                <div class="tool-editor-panel-head compact">
                  <b>渲染结果</b>
                  <span>{{ promptPreviewResult?.success === false ? '失败' : 'Preview' }}</span>
                </div>
                <pre>{{ promptPreviewResult?.renderedContent || promptPreviewResult?.errorMessage || '点击「预览渲染」后显示 prompts/get 最终返回的文本' }}</pre>
              </aside>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioSkillEdit'">
          <section class="tool-editor-shell">
            <div class="api-editor-header">
              <div>
                <button type="button" class="api-back-btn" @click="changePage('studioSkills')">← 返回列表</button>
                <span class="api-section-kicker">Cursor Skill</span>
                <h2>{{ model.id ? '编辑 Skill' : '新建 Skill' }}</h2>
                <p>Skill 是可安装的任务说明包。当前先支持单文件 SKILL.md，Agent 通过 get_skill 获取后写入 Cursor Skills 目录。</p>
              </div>
              <aside>
                <span>当前 Skill</span>
                <b>{{ model.skillCode || '保存时生成 Skill ID' }}</b>
                <small>{{ Number(model.publishStatus) === 0 ? '草稿状态，发布后 get_skill 才能读取' : `${publishLabel(model.publishStatus)}，${visibilityLabel(model.publishStatus)}` }}</small>
              </aside>
            </div>

            <div class="tool-editor-board resource-editor-board">
              <aside class="tool-editor-meta">
                <div class="tool-editor-panel-head">
                  <b>基本信息</b>
                  <span>Skill ID 用于 get_skill 安装</span>
                </div>
                <label>
                  <span>Skill ID</span>
                  <input v-model="model.skillCode" placeholder="保存时自动生成" readonly />
                </label>
                <label>
                  <span>名称</span>
                  <input v-model="model.name" placeholder="如 作业合规检查" />
                </label>
                <label>
                  <span>描述</span>
                  <textarea v-model="model.description" spellcheck="false" placeholder="说明这个 Skill 适合什么任务场景"></textarea>
                </label>
                <label>
                  <span>分类</span>
                  <input v-model="model.category" placeholder="如 教学教务 / 研发提效" />
                </label>

                <div class="tool-editor-panel-head compact">
                  <b>SKILL.md 文件</b>
                  <span>{{ model.fileName || '未上传' }}</span>
                </div>
                <label class="resource-upload-box">
                  <input type="file" accept=".md,text/markdown" @change="onSkillFileChange" />
                  <span><DatabaseOutlined />选择 SKILL.md</span>
                  <small>{{ skillSelectedFile?.name || model.fileName || '仅支持 .md，建议小于 2MB' }}</small>
                </label>
              </aside>

              <section class="tool-script-editor">
                <div class="tool-editor-panel-head">
                  <b>Skill 预览</b>
                  <span>{{ skillPreviewLoading ? '读取中' : '安装时会自动补齐 frontmatter' }}</span>
                </div>
                <div class="resource-storage-panel">
                  <div>
                    <span>安装目录</span>
                    <b>{{ model.name || model.skillCode || '-' }}</b>
                  </div>
                  <div>
                    <span>文件大小</span>
                    <b>{{ model.fileSize ? `${Math.ceil(model.fileSize / 1024)} KB` : '-' }}</b>
                  </div>
                  <div>
                    <span>存储状态</span>
                    <code>{{ model.objectKey ? '已上传到对象存储' : '保存时自动上传到对象存储' }}</code>
                  </div>
                </div>
                <div class="resource-markdown-preview" v-html="skillPreviewHtml"></div>
                <div class="tool-editor-actions">
                  <button type="button" class="tool-save-btn" :disabled="skillSaving" @click="saveSkillEditor">{{ skillSaving ? '保存中' : '保存 Skill' }}</button>
                  <button v-if="model.id" type="button" class="tool-run-btn" :disabled="skillPreviewLoading" @click="loadSkillPreview()">
                    {{ skillPreviewLoading ? '读取中' : '刷新预览' }}
                  </button>
                  <button v-if="model.id" type="button" class="tool-run-btn" :disabled="Number(model.publishStatus) === 0" @click="openCursorSkillInstall(model)">
                    Cursor 安装
                  </button>
                  <button v-if="model.id" type="button" class="tool-run-btn" :disabled="Number(model.publishStatus) === 0" @click="downloadSkillZip(model)">
                    下载 ZIP
                  </button>
                  <button v-if="model.id" type="button" class="tool-online-btn" @click="toggleOnlineSkill(model)">
                    {{ onlineActionLabel(model.publishStatus) }}
                  </button>
                  <button v-if="model.id" type="button" class="tool-online-btn" :disabled="Number(model.publishStatus) === 0" @click="toggleVisibilitySkill(model)">
                    {{ visibilityActionLabel(model.publishStatus) }}
                  </button>
                </div>
              </section>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioResourceEdit'">
          <section class="tool-editor-shell">
            <div class="api-editor-header">
              <div>
                <button type="button" class="api-back-btn" @click="changePage('studioResources')">← 返回列表</button>
                <span class="api-section-kicker">MCP Resource</span>
                <h2>{{ model.id ? '编辑 Resource' : '新建 Resource' }}</h2>
                <p>Resource 负责保存可读取的企业上下文。当前先支持 Markdown 文件，内容由 Agent 通过 resources/read 获取。</p>
              </div>
              <aside>
                <span>当前资源</span>
                <b>{{ model.resourceUri || '未命名 Resource' }}</b>
                <small>{{ Number(model.publishStatus) === 0 ? '草稿状态，发布后才能进入权限链路' : `${publishLabel(model.publishStatus)}，${visibilityLabel(model.publishStatus)}` }}</small>
              </aside>
            </div>

            <div class="tool-editor-board resource-editor-board">
              <aside class="tool-editor-meta">
                <div class="tool-editor-panel-head">
                  <b>基本信息</b>
                  <span>Resource URI 会出现在 resources/list</span>
                </div>
                <label>
                  <span>Resource URI</span>
                  <input v-model="model.resourceUri" placeholder="如 bear://docs/homework_rules" />
                </label>
                <label>
                  <span>名称</span>
                  <input v-model="model.name" placeholder="如 作业提交规则" />
                </label>
                <label>
                  <span>描述</span>
                  <textarea v-model="model.description" spellcheck="false" placeholder="说明这份资源适合什么场景读取"></textarea>
                </label>

                <div class="tool-editor-panel-head compact">
                  <b>Markdown 文件</b>
                  <span>{{ model.fileName || '未上传' }}</span>
                </div>
                <label class="resource-upload-box">
                  <input type="file" accept=".md,text/markdown" @change="onResourceFileChange" />
                  <span><DatabaseOutlined />选择 Markdown 文件</span>
                  <small>{{ resourceSelectedFile?.name || model.fileName || '仅支持 .md，建议小于 2MB' }}</small>
                </label>
              </aside>

              <section class="tool-script-editor">
                <div class="tool-editor-panel-head">
                  <b>文档预览</b>
                  <span>{{ resourcePreviewLoading ? '读取中' : '通过预签名地址读取 Markdown 内容' }}</span>
                </div>
                <div class="resource-storage-panel">
                  <div>
                    <span>MIME 类型</span>
                    <b>{{ model.mimeType || 'text/markdown' }}</b>
                  </div>
                  <div>
                    <span>文件大小</span>
                    <b>{{ model.fileSize ? `${Math.ceil(model.fileSize / 1024)} KB` : '-' }}</b>
                  </div>
                  <div>
                    <span>存储状态</span>
                    <code>{{ model.objectKey ? '已上传到对象存储' : '保存时自动上传到对象存储' }}</code>
                  </div>
                </div>
                <div class="resource-markdown-preview" v-html="resourcePreviewHtml"></div>
                <div class="tool-editor-actions">
                  <button type="button" class="tool-save-btn" :disabled="resourceSaving" @click="saveResourceEditor">{{ resourceSaving ? '保存中' : '保存 Resource' }}</button>
                  <button v-if="model.id" type="button" class="tool-run-btn" :disabled="resourcePreviewLoading" @click="loadResourcePreview()">
                    {{ resourcePreviewLoading ? '读取中' : '刷新预览' }}
                  </button>
                  <button v-if="model.id" type="button" class="tool-online-btn" @click="toggleOnlineResource(model)">
                    {{ onlineActionLabel(model.publishStatus) }}
                  </button>
                  <button v-if="model.id" type="button" class="tool-online-btn" :disabled="Number(model.publishStatus) === 0" @click="toggleVisibilityResource(model)">
                    {{ visibilityActionLabel(model.publishStatus) }}
                  </button>
                </div>
              </section>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioToolEdit'">
          <section class="tool-editor-shell">
            <div class="api-editor-header">
              <div>
                <button type="button" class="api-back-btn" @click="changePage('studioTools')">← 返回列表</button>
                <span class="api-section-kicker">Dynamic Tool</span>
                <h2>{{ model.id ? '编辑 Tool' : '新建 Tool' }}</h2>
                <p>Tool 创作负责包装能力：通过 inputSchema 定义入参，通过 Groovy 编排逻辑，并用 <code>runRequest.runRequest</code> 或 <code>runSql.runSql</code> 调用已保存的能力配置。</p>
              </div>
              <aside>
                <span>当前工具</span>
                <b>{{ model.toolName || '未命名 Tool' }}</b>
                <small>{{ Number(model.publishStatus) === 0 ? '草稿状态，可先调试再上线' : `${publishLabel(model.publishStatus)}，${visibilityLabel(model.publishStatus)}` }}</small>
              </aside>
            </div>

            <div class="tool-editor-board">
              <aside class="tool-editor-meta">
                <div class="tool-editor-panel-head">
                  <b>Tool 基本信息</b>
                  <span>Agent 会根据名称、描述和 Schema 选择工具</span>
                </div>
                <label>
                  <span>工具名</span>
                  <input v-model="model.toolName" placeholder="如 query_course_list_tool" />
                </label>
                <label>
                  <span>工具描述</span>
                  <textarea v-model="model.toolDescription" spellcheck="false" placeholder="说明这个工具适合解决什么问题"></textarea>
                </label>
                <label>
                  <span>inputSchema</span>
                  <textarea v-model="model.inputSchema" class="tool-code-input" spellcheck="false"></textarea>
                </label>
                <div class="tool-editor-panel-head compact">
                  <b>API 白名单</b>
                  <span>已选 {{ selectedToolRequestKeys.length }} 个 configKey</span>
                </div>
                <a-select
                  v-model:value="selectedToolRequestKeys"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="toolApiOptions"
                  option-filter-prop="label"
                  placeholder="搜索并选择可调用的 API configKey"
                  class="tool-api-select"
                  popupClassName="dark-select-dropdown tool-api-dropdown"
                  max-tag-count="responsive"
                />
                <p v-if="!availableToolApis.length" class="tool-api-empty">暂无已上线 API，请先在 API 创作中上线一个配置。</p>
                <div class="tool-editor-panel-head compact">
                  <b>数据源白名单</b>
                  <span>已选 {{ selectedToolDataSourceIds.length }} 个 datasourceId</span>
                </div>
                <a-select
                  v-model:value="selectedToolDataSourceIds"
                  mode="multiple"
                  show-search
                  allow-clear
                  :options="toolDataSourceOptions"
                  option-filter-prop="label"
                  placeholder="搜索并选择可查询的数据源"
                  class="tool-api-select"
                  popupClassName="dark-select-dropdown tool-api-dropdown"
                  max-tag-count="responsive"
                />
                <p v-if="!availableToolDataSources.length" class="tool-api-empty">暂无已发布数据源，请先在管理后台发布一个数据源。</p>
              </aside>

              <section class="tool-script-editor">
                <div class="tool-editor-panel-head">
                  <b>Groovy 脚本</b>
                  <span class="tool-head-actions">
                    <button type="button" @click="generateToolSchemaFromScript">从脚本生成 Schema</button>
                    <button type="button" @click="generateToolAssetsFromDataSourceSelection">按已选数据源生成</button>
                    <button type="button" @click="generateToolAssetsFromSelection">按已选 API 生成</button>
                  </span>
                </div>
                <textarea v-model="model.groovyScript" spellcheck="false"></textarea>
                <div class="tool-editor-actions">
                  <button type="button" class="tool-save-btn" :disabled="toolSaving" @click="saveToolEditor">{{ toolSaving ? '保存中' : '保存 Tool' }}</button>
                  <button type="button" class="tool-run-btn" :disabled="toolDebugLoading" @click="sendToolEditor">{{ toolDebugLoading ? '运行中' : '运行调试' }}</button>
                  <button v-if="model.id" type="button" class="tool-online-btn" @click="toggleOnlineTool(model)">
                    {{ onlineActionLabel(model.publishStatus) }}
                  </button>
                  <button v-if="model.id" type="button" class="tool-online-btn" :disabled="Number(model.publishStatus) === 0" @click="toggleVisibilityTool(model)">
                    {{ visibilityActionLabel(model.publishStatus) }}
                  </button>
                </div>
              </section>

              <aside class="tool-debug-panel">
                <div class="tool-editor-panel-head">
                  <b>调试面板</b>
                  <span>输入 params，查看脚本返回值或错误信息</span>
                </div>
                <label>
                  <span>测试参数 JSON</span>
                  <textarea v-model="toolDebugParams" spellcheck="false"></textarea>
                </label>
                <div class="tool-debug-result-head">
                  <b>执行结果</b>
                  <span v-if="toolDebugResult" :class="toolDebugResult.success ? 'success' : 'error'">{{ toolDebugResult.success ? '成功' : '失败' }} · {{ toolDebugResult.durationMs ?? '-' }} ms</span>
                  <span v-else>等待调试</span>
                </div>
                <pre>{{ toolDebugText }}</pre>
              </aside>
            </div>
          </section>
        </template>

        <template v-else-if="page === 'studioApiEdit'">
          <section class="api-editor-shell">
            <div class="api-editor-header">
              <div>
                <button type="button" class="api-back-btn" @click="changePage('studioApis')">← 返回列表</button>
                <span class="api-section-kicker">HTTP Request</span>
                <h2>{{ model.id ? '编辑 API' : '新建 API' }}</h2>
                <p>配置外部 HTTP API，使用 <code v-text="'{{key}}'"></code> 占位符完成参数替换，保存后可直接发送调试。</p>
              </div>
              <aside>
                <span>当前配置</span>
                <b>{{ model.configKey || '未命名配置' }}</b>
                <small>{{ model.url || '等待填写请求地址' }}</small>
              </aside>
            </div>

            <div class="api-editor-board">
              <div class="api-editor-meta">
                <label>
                  <span>协议类型</span>
                  <select v-model="model.type">
                    <option value="HTTP">HTTP</option>
                  </select>
                </label>
                <label>
                  <span>配置键</span>
                  <input v-model="model.configKey" placeholder="如 ylog_search，用于 runRequest 调用" />
                </label>
                <label>
                  <span>接口名称</span>
                  <input v-model="model.name" placeholder="接口名称" />
                </label>
              </div>

              <div class="api-request-line">
                <select v-model="model.method">
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                </select>
                <input v-model="model.url" :placeholder="'输入请求 URL，支持 {{key}} 占位符'" />
                <button type="button" class="api-send-btn" :disabled="debugLoading" @click="sendApiEditor">发送</button>
                <button type="button" class="api-save-btn" :disabled="apiSaving" @click="saveApiEditor">
                  {{ apiSaving ? '保存中' : '保存' }}
                </button>
              </div>

              <div class="api-editor-tabs">
                <button v-for="tab in ['params','headers','body','settings']" :key="tab" :class="{active: apiEditorTab === tab}" @click="apiEditorTab = tab">
                  {{ { params:'Params', headers:'Headers', body:'Body', settings:'Settings' }[tab] }}
                </button>
              </div>

              <div class="api-editor-main">
                <div class="api-editor-panel">
                  <template v-if="apiEditorTab === 'params'">
                    <div class="api-params-split">
                      <section>
                        <div class="api-panel-head">
                          <b>默认参数 JSON</b>
                          <span>保存到配置，后续 runRequest 默认带上</span>
                        </div>
                        <textarea v-model="model.paramsDefault" spellcheck="false" placeholder="填写 JSON 对象，例如 pageSize、token、默认查询条件"></textarea>
                      </section>
                      <section>
                        <div class="api-panel-head">
                          <b>本次调试参数 JSON</b>
                          <button type="button" @click="apiEditorDebugParams = model.paramsDefault || '{}'">使用默认参数</button>
                        </div>
                        <textarea v-model="apiEditorDebugParams" spellcheck="false" placeholder="只用于本次点击发送，可覆盖默认参数里的同名字段"></textarea>
                      </section>
                    </div>
                  </template>
                  <template v-else-if="apiEditorTab === 'headers'">
                    <div class="api-panel-head"><b>请求头 JSON</b><button type="button">格式化</button></div>
                    <textarea v-model="model.headers" spellcheck="false" placeholder="填写 JSON 对象，例如 Authorization、Content-Type"></textarea>
                  </template>
                  <template v-else-if="apiEditorTab === 'body'">
                    <div class="api-panel-head"><b>Body 模板，支持 <code v-text="'{{key}}'"></code> 占位符</b><button type="button">格式化</button></div>
                    <textarea v-model="model.bodyTemplate" spellcheck="false" placeholder="填写 POST/PUT 请求体模板，可使用占位符"></textarea>
                  </template>
                  <template v-else>
                    <div class="api-settings-grid">
                      <label><span>连接超时 (ms)</span><input v-model.number="model.connectTimeoutMs" type="number" min="0" /></label>
                      <label><span>读取超时 (ms)</span><input v-model.number="model.readTimeoutMs" type="number" min="0" /></label>
                      <label><span>每分钟限流</span><input v-model.number="model.rateLimitPerMinute" type="number" min="0" /></label>
                      <label><span>分类</span><input v-model="model.category" placeholder="例如：search" /></label>
                    </div>
                    <label class="api-description-field">
                      <span>接口描述</span>
                      <textarea v-model="model.description" spellcheck="false" placeholder="简要说明接口用途"></textarea>
                    </label>
                  </template>
                </div>

                <aside class="api-response-panel">
                  <div class="api-response-head">
                    <b>响应输出</b>
                    <span v-if="debugResult" :class="['api-debug-state', debugResult.success ? 'success' : 'error']">
                      {{ debugResult.success ? '调试成功' : '调试失败' }}
                      <small v-if="debugResult.durationMs != null">{{ debugResult.durationMs }} ms</small>
                    </span>
                    <span v-else>状态码 · 响应体 · 耗时</span>
                  </div>
                  <div v-if="debugResult && !debugResult.success" class="api-debug-error">
                    {{ debugResult.errorMessage || '调试失败，请检查请求配置' }}
                  </div>
                  <div v-if="debugSummary" class="api-response-summary">
                    <div>
                      <span>HTTP 状态</span>
                      <b>{{ debugSummary.status ?? '-' }}</b>
                    </div>
                    <div>
                      <span>耗时</span>
                      <b>{{ debugSummary.durationMs ?? '-' }} ms</b>
                    </div>
                  </div>
                  <div class="api-response-body-head">
                    <b>响应体</b>
                    <span>{{ debugResult?.result?.body ? '已格式化' : '等待调试' }}</span>
                  </div>
                  <pre>{{ debugBodyText }}</pre>
                </aside>
              </div>
            </div>
          </section>
        </template>
      </main>
    </div>
  </template>
  <template v-else>
  <a-layout class="console-layout">
    <a-layout-sider width="260" class="console-sider">
      <div class="brand"><span class="brand-mark">B</span><span>Bear MCP<small>管理控制台</small></span></div>
      <a-menu theme="dark" mode="inline" :selected-keys="[page]" class="console-menu" @click="({key}) => changePage(key)">
        <div class="nav-caption">概览</div>
        <a-menu-item key="dashboard"><AppstoreOutlined /><span>概览</span></a-menu-item>
        <div class="nav-caption">系统治理</div>
        <a-menu-item key="users"><TeamOutlined /><span>用户</span></a-menu-item>
        <a-menu-item key="roles"><SafetyCertificateOutlined /><span>角色与能力权限</span></a-menu-item>
        <div class="nav-caption">访问控制</div>
        <a-menu-item key="tokens"><KeyOutlined /><span>Token 与能力选择</span></a-menu-item>
        <div class="nav-caption">能力展示</div>
        <a-menu-item key="requests"><SettingOutlined /><span>请求配置</span></a-menu-item>
        <a-menu-item key="dataSources"><DatabaseOutlined /><span>数据源</span></a-menu-item>
        <a-menu-item key="resources"><DatabaseOutlined /><span>资源</span></a-menu-item>
        <a-menu-item key="tools"><ThunderboltOutlined /><span>动态工具</span></a-menu-item>
        <div class="nav-caption">运行观测</div>
        <a-menu-item key="audits"><AuditOutlined /><span>审计日志</span></a-menu-item>
      </a-menu>
      <div class="sider-footer"><span class="live-dot"></span><span>MCP Server Online</span><small>v0.1.0 · Admin Console</small></div>
    </a-layout-sider>
    <a-layout-content class="layout-content">
      <header class="console-topbar"><div class="top-search"><SearchOutlined /><span>搜索页面、工具或配置</span><kbd>⌘ K</kbd></div><div class="top-actions"><a-button @click="changePage('shareHome')">Bear 社区</a-button><a-button @click="changePage('studioHome')">创作空间</a-button><a-button type="text" shape="circle" :icon="h(BellOutlined)" /><div class="user-chip"><span class="avatar">D</span><span><b>demo-admin</b><small>管理员</small></span></div></div></header>
      <div class="page-head"><div><div class="breadcrumb">MCP 管理后台 <span>/</span> {{ navSections[page] }}</div><h1 class="page-title">{{ title }}</h1><div class="page-desc">{{ desc }}</div></div><a-button v-if="['users','roles','tokens','requests','dataSources'].includes(page)" type="primary" class="create-btn" :icon="h(PlusOutlined)" @click="openCreate">新建{{ title.replace('与工具权限','').replace('与工具选择','').replace('与能力权限','').replace('与能力选择','') }}</a-button></div>
      <template v-if="page === 'dashboard'">
        <a-row :gutter="16" class="metric-grid"><a-col v-for="[label,key,icon,color,note] in [['用户', 'users', TeamOutlined, 'violet', '当前数据库统计'],['角色','roles',SafetyCertificateOutlined, 'cyan', '当前数据库统计'],['有效 Token','activeTokens',KeyOutlined, 'orange', '当前数据库统计'],['启用请求','enabledRequests',DatabaseOutlined, 'green', '当前数据库统计'],['动态工具','enabledDynamicTools',ThunderboltOutlined, 'pink', '当前数据库统计'],['今日调用','todayCalls',AuditOutlined, 'cyan', '今日审计统计']]" :key="key" :span="4"><a-card class="metric"><div class="metric-top"><span>{{ label }}</span><span :class="['metric-icon', color]"><component :is="icon" /></span></div><a-statistic :value="dashboard[key] || 0" /><div class="metric-note"><span class="trend">●</span> {{ note }}</div></a-card></a-col></a-row>
        <div class="surface dashboard-table"><div class="table-toolbar"><div><b>最近调用</b><small>最新 100 条 MCP 工具调用记录</small></div><a-button @click="load">刷新数据</a-button></div><a-table :data-source="rows" :columns="[{title:'时间',dataIndex:'createTime'},{title:'工具',dataIndex:'toolName'},{title:'用户',dataIndex:'userName'},{title:'状态',dataIndex:'status'},{title:'耗时(ms)',dataIndex:'durationMs'}]" row-key="id" :pagination="false" /></div>
      </template>
      <template v-else-if="page === 'studioApis'">
        <div class="studio-hero">
          <div>
            <span>API Workspace</span>
            <h2>先把外部 HTTP API 接进来</h2>
            <p>这里负责 API 录入、参数模板、在线调试和发布。动态工具包装放到下一步，页面边界先立住。</p>
          </div>
          <div class="studio-flow">
            <b>保存配置</b>
            <i></i>
            <b>调试 HTTP</b>
            <i></i>
            <b>发布能力</b>
          </div>
        </div>

        <div class="studio-api-grid">
          <article v-for="item in rows" :key="item.id" class="studio-api-card">
            <div class="studio-api-card-head">
              <div>
                <span class="api-method">{{ item.method || 'GET' }}</span>
                <h3>{{ item.name || item.configKey }}</h3>
              </div>
              <div class="studio-api-badges">
                <em :class="['publish-badge', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</em>
                <em v-if="Number(item.publishStatus) !== 0" :class="['publish-badge', visibilityClass(item.publishStatus)]">{{ visibilityLabel(item.publishStatus) }}</em>
              </div>
            </div>
            <p>{{ item.description || '暂无 API 描述' }}</p>
            <div class="api-meta">
              <span>{{ item.configKey }}</span>
              <span>{{ item.category || '未分类' }}</span>
              <span>{{ Number(item.isEnabled) === 1 ? '启用' : '禁用' }}</span>
            </div>
            <code>{{ item.url }}</code>
            <div class="studio-api-actions">
              <a-button @click="edit(item)">编辑</a-button>
              <a-button @click="openApiDebug(item)">调试</a-button>
              <a-button :type="Number(item.publishStatus) === 0 ? 'primary' : 'default'" :danger="Number(item.publishStatus) !== 0" @click="toggleOnlineApi(item)">{{ onlineActionLabel(item.publishStatus) }}</a-button>
              <a-button :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityApi(item)">{{ visibilityActionLabel(item.publishStatus) }}</a-button>
            </div>
          </article>
        </div>
        <a-empty v-if="!loading && !rows.length" description="还没有 API，先新建一个外部 HTTP API" :image-style="{height:'56px'}" />
      </template>
      <template v-else><div class="surface"><div class="table-toolbar"><div><b>{{ title }}列表</b><small>共 {{ rows.length }} 条记录<span v-if="page==='tools' || page==='resources'"> · 由创作空间发布</span></small></div><div class="table-tools"><a-input placeholder="搜索名称或编码" class="table-search"><template #prefix><SearchOutlined /></template></a-input><a-button @click="load">刷新</a-button></div></div><a-table :loading="loading" :data-source="rows" :columns="[...dataColumns,{title:'操作',key:'action'}]" row-key="id"><template #bodyCell="{column,record}"><template v-if="column.key==='action'"><a-button v-if="page==='users'" type="link" @click="updateUserRoles(record)">分配角色</a-button><a-button v-if="page==='roles'" type="link" @click="updateRoleTools(record)">配置工具</a-button><a-button v-if="page==='roles'" type="link" @click="updateRolePrompts(record)">配置 Prompt</a-button><a-button v-if="page==='roles'" type="link" @click="updateRoleResources(record)">配置 Resource</a-button><a-button v-if="page==='tokens'" type="link" @click="updateSelections(record)">工具选择</a-button><a-button v-if="page==='tokens'" type="link" @click="updateTokenPromptSelections(record)">Prompt 选择</a-button><a-button v-if="page==='tokens'" type="link" @click="updateTokenResourceSelections(record)">Resource 选择</a-button><a-button v-if="page==='tools'" type="link" @click="showDynamicToolDetail(record)">查看详情</a-button><a-button v-if="page==='resources'" type="link" @click="openResourceEditor(record)">查看资源</a-button><a-button v-if="page==='resources'" type="link" :danger="Number(record.enabled) === 1" @click="toggleResourceEnabled(record)">{{ Number(record.enabled) === 1 ? '禁用' : '启用' }}</a-button><a-button v-if="page==='audits'" type="link" @click="showAuditDetail(record)">查看详情</a-button><a-button v-if="!['tools','resources','audits'].includes(page)" type="link" @click="edit(record)">编辑</a-button><a-button v-if="page==='dataSources'" type="link" danger @click="removeRow(record)">删除</a-button></template></template></a-table></div></template>
    </a-layout-content>
  </a-layout>
  </template>
  <a-drawer v-model:open="drawer" :title="drawerTitle" :width="['requests','studioApis','dataSources'].includes(page) ? 760 : 600" class="console-drawer">
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
    <a-form v-else-if="page === 'users'" layout="vertical" class="entity-form">
      <section class="form-section">
        <div class="form-section-head">
          <b>用户信息</b>
          <span>登录身份和后台展示名称</span>
        </div>

        <a-form-item label="登录用户名">
          <a-input v-model:value="model.username" size="large" placeholder="例如：demo-admin" />
        </a-form-item>

        <a-form-item label="展示名称">
          <a-input v-model:value="model.displayName" size="large" placeholder="例如：演示管理员" />
        </a-form-item>

        <a-form-item v-if="!model.id" label="初始密码">
          <a-input-password v-model:value="model.password" size="large" placeholder="不填则使用课堂默认密码 123456" />
          <div class="form-help">编辑用户时当前版本不在这里修改密码。</div>
        </a-form-item>
      </section>

      <section class="form-section compact">
        <a-form-item label="账号状态">
          <a-segmented
            v-model:value="model.isEnabled"
            :options="activeStatusOptions"
            block
            class="status-segmented"
          />
        </a-form-item>
      </section>
    </a-form>
    <a-form v-else-if="page === 'roles'" layout="vertical" class="entity-form">
      <section class="form-section">
        <div class="form-section-head">
          <b>角色信息</b>
          <span>角色决定用户可以使用哪些工具的资格上限</span>
        </div>

        <a-form-item label="角色编码">
          <a-input v-model:value="model.roleCode" size="large" placeholder="例如：ADMIN" />
        </a-form-item>

        <a-form-item label="角色名称">
          <a-input v-model:value="model.roleName" size="large" placeholder="例如：管理员" />
        </a-form-item>

        <a-form-item label="角色说明">
          <a-textarea v-model:value="model.description" class="code-area" placeholder="说明这个角色适合哪些用户" :auto-size="{minRows:3,maxRows:5}" />
        </a-form-item>
      </section>

      <section class="form-section compact">
        <a-form-item label="角色状态">
          <a-segmented
            v-model:value="model.isEnabled"
            :options="activeStatusOptions"
            block
            class="status-segmented"
          />
        </a-form-item>
      </section>
    </a-form>
    <a-form v-else-if="page === 'requests' || page === 'studioApis'" layout="vertical" class="entity-form request-form">
      <section class="form-section">
        <div class="form-section-head">
          <b>基础信息</b>
          <span>{{ page === 'studioApis' ? '先保存外部 HTTP API，后续可包装成动态工具' : '动态工具通过配置 Key 引用这项能力' }}</span>
        </div>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="接口 ID">
              <a-input v-model:value="model.requestId" size="large" placeholder="例如：API0000000001" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="配置 Key">
              <a-input v-model:value="model.configKey" size="large" placeholder="例如：demo_clock" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="配置名称">
          <a-input v-model:value="model.name" size="large" placeholder="例如：查询当前时间" />
        </a-form-item>

        <a-form-item label="能力说明">
          <a-textarea v-model:value="model.description" class="code-area" placeholder="说明这项请求配置提供什么企业能力" :auto-size="{minRows:3,maxRows:5}" />
        </a-form-item>
      </section>

      <section class="form-section">
        <div class="form-section-head">
          <b>调用方式</b>
          <span>当前支持外部 HTTP API 接入</span>
        </div>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="协议类型">
              <a-select v-model:value="model.type" :options="requestTypeOptions" size="large" popupClassName="dark-select-dropdown" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="HTTP 方法">
              <a-select v-model:value="model.method" :options="httpMethodOptions" size="large" popupClassName="dark-select-dropdown" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="分类">
              <a-input v-model:value="model.category" size="large" placeholder="例如：demo" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="请求 URL">
          <a-input v-model:value="model.url" size="large" placeholder="外部 HTTP API 请求地址" />
        </a-form-item>

        <a-row v-if="page === 'requests'" :gutter="12">
          <a-col :span="12">
            <a-form-item label="服务名称">
              <a-input v-model:value="model.serviceName" size="large" placeholder="SOA/Hessian 服务名，可留空" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="方法名称">
              <a-input v-model:value="model.methodName" size="large" placeholder="SOA/Hessian 方法名，可留空" />
            </a-form-item>
          </a-col>
        </a-row>
      </section>

      <section class="form-section">
        <div class="form-section-head">
          <b>参数与治理</b>
          <span>JSON 字段用于脚本调试和运行时默认参数</span>
        </div>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="请求头 JSON">
              <a-textarea v-model:value="model.headers" class="code-area" :auto-size="{minRows:4,maxRows:8}" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="默认参数 JSON">
              <a-textarea v-model:value="model.paramsDefault" class="code-area" :auto-size="{minRows:4,maxRows:8}" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="请求体模板">
          <a-textarea v-model:value="model.bodyTemplate" class="code-area" placeholder="POST/PUT 请求体模板，可留空" :auto-size="{minRows:3,maxRows:8}" />
        </a-form-item>

        <a-form-item v-if="page === 'requests'" label="参数 Schema JSON">
          <a-textarea v-model:value="model.argsSchema" class="code-area" :auto-size="{minRows:3,maxRows:8}" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="连接超时(ms)">
              <a-input-number v-model:value="model.connectTimeoutMs" size="large" :min="0" class="full-number" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="读取超时(ms)">
              <a-input-number v-model:value="model.readTimeoutMs" size="large" :min="0" class="full-number" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="每分钟限流">
              <a-input-number v-model:value="model.rateLimitPerMinute" size="large" :min="0" class="full-number" />
            </a-form-item>
          </a-col>
        </a-row>
      </section>

      <section class="form-section compact">
        <a-row :gutter="12">
          <a-col v-if="page === 'requests'" :span="12">
            <a-form-item label="启用状态">
              <a-segmented v-model:value="model.isEnabled" :options="activeStatusOptions" block class="status-segmented" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="发布状态">
              <a-segmented v-model:value="model.publishStatus" :options="publishStatusOptions" block class="status-segmented publish-segmented" />
            </a-form-item>
          </a-col>
        </a-row>
      </section>
    </a-form>
    <a-form v-else-if="page === 'dataSources'" layout="vertical" class="entity-form request-form">
      <section class="form-section">
        <div class="form-section-head">
          <b>基础信息</b>
          <span>数据源发布后可作为 MCP 查询能力和动态 Tool runSql 的配置来源</span>
        </div>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="数据源名称">
              <a-input v-model:value="model.name" size="large" placeholder="例如：课堂演示库" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数据源 Key">
              <a-input v-model:value="model.datasourceKey" size="large" :disabled="Boolean(model.id)" placeholder="例如：classroom_demo" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="数据源说明">
          <a-textarea v-model:value="model.description" class="code-area" placeholder="说明这个库包含哪些业务数据、适合哪些查询场景" :auto-size="{minRows:3,maxRows:5}" />
        </a-form-item>
      </section>

      <section class="form-section">
        <div class="form-section-head">
          <b>连接信息</b>
          <span>密码只会保存为密文，列表和编辑接口不会返回明文</span>
        </div>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="数据库类型">
              <a-select v-model:value="model.dbType" :options="dataSourceDbTypeOptions" size="large" popupClassName="dark-select-dropdown" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="用户名">
              <a-input v-model:value="model.username" size="large" placeholder="数据库用户名" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="model.id ? '密码（留空不修改）' : '密码'">
              <a-input-password v-model:value="model.password" size="large" placeholder="数据库密码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="JDBC URL">
          <a-input v-model:value="model.jdbcUrl" size="large" placeholder="jdbc:mysql://localhost:3306/demo" />
        </a-form-item>

        <a-form-item label="额外 JDBC 参数 JSON">
          <a-textarea v-model:value="model.extraJdbcProps" class="code-area" :auto-size="{minRows:3,maxRows:6}" placeholder='例如：{"useUnicode":"true"}' />
        </a-form-item>
      </section>

      <section class="form-section compact">
        <a-form-item label="发布状态">
          <a-segmented v-model:value="model.publishStatus" :options="dataSourcePublishStatusOptions" block class="status-segmented publish-segmented" />
        </a-form-item>
      </section>
    </a-form>
    <a-form v-else layout="vertical" class="entity-form">
      <template v-for="(value,key) in model" :key="key"><a-form-item v-if="!['id','tokenHash','tokenPrefix','createTime','updateTime','lastUsedTime','lastUsedIp'].includes(key)" :label="key"><a-textarea v-if="['headers','bodyTemplate','paramsDefault','inputSchema','groovyScript','linkedRequestKeys','linkedDataSourceIds','description','argsSchema'].includes(key)" v-model:value="model[key]" class="code-area" :auto-size="{minRows:2,maxRows:8}" /><a-input v-else v-model:value="model[key]" /></a-form-item></template>
    </a-form>
    <template #footer><a-space><a-button @click="drawer=false">取消</a-button><a-button v-if="page === 'dataSources'" :loading="dataSourceTesting" @click="testDataSourceConnection">测试连接</a-button><a-button type="primary" @click="save">保存</a-button></a-space></template>
  </a-drawer>
  <a-modal
    v-model:open="oneTimeTokenOpen"
    title="Token 创建成功"
    width="720px"
    class="tool-permission-modal one-time-token-modal"
    :maskClosable="false"
    @cancel="closeOneTimeTokenModal"
  >
    <section class="tool-option-section">
      <div class="tool-option-title">
        <div>
          <b>{{ oneTimeTokenName }}</b>
          <small>完整 Token 只展示一次，关闭后无法再次查看</small>
        </div>
      </div>
      <p class="permission-hint detail-desc">请立即复制并保存到 MCP Client 或安全的密钥管理位置。数据库只保存 Token Hash 和展示前缀，后台无法找回完整明文。</p>
      <pre class="detail-code token-plain-code">{{ rawToken }}</pre>
    </section>
    <template #footer>
      <a-space>
        <a-button @click="copyRawToken">复制 Token</a-button>
        <a-button type="primary" @click="closeOneTimeTokenModal">我已保存</a-button>
      </a-space>
    </template>
  </a-modal>
  <a-modal v-model:open="apiDebugOpen" :title="`调试 API · ${activeStudioApi?.name || ''}`" width="860px" class="tool-permission-modal dynamic-tool-detail-modal">
    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot builtin"></span>
        调试参数
        <small>JSON 对象，会覆盖默认参数中的同名字段</small>
      </div>
      <a-textarea v-model:value="debugParams" class="code-area debug-textarea" :auto-size="{minRows:6,maxRows:12}" />
      <div class="debug-actions">
        <a-button type="primary" :loading="debugLoading" @click="runApiDebug">发送请求</a-button>
      </div>
    </section>

    <section v-if="debugResult" class="tool-option-section">
      <div class="tool-option-title">
        <span :class="['tool-type-dot', debugResult.success ? 'builtin' : 'dynamic']"></span>
        调试结果
        <small>{{ debugResult.durationMs != null ? `${debugResult.durationMs} ms` : '' }}</small>
      </div>
      <pre class="detail-code script-code">{{ JSON.stringify(debugResult, null, 2) }}</pre>
    </section>

    <template #footer>
      <a-button @click="apiDebugOpen=false">关闭</a-button>
    </template>
  </a-modal>
  <a-modal v-model:open="communityDetailOpen" :title="communityDetailTitle" width="760px" class="tool-permission-modal dynamic-tool-detail-modal community-detail-modal">
    <template v-if="activeCommunityItem?.communityKind === 'tool'">
      <section class="tool-option-section">
        <div class="tool-option-title">
          <span class="tool-type-dot dynamic"></span>
          基础信息
          <small>{{ activeCommunityItem?.communityType === 'builtin' ? '内置工具' : '动态工具' }}</small>
        </div>
        <div class="detail-grid">
          <div>
            <span>工具名</span>
            <b>{{ activeCommunityItem?.displayName || activeCommunityItem?.toolName || '-' }}</b>
          </div>
          <div>
            <span>工具编号</span>
            <b>{{ communityToolCode(activeCommunityItem || {}) }}</b>
          </div>
          <div>
            <span>分类</span>
            <b>{{ activeCommunityItem?.displayCategory || activeCommunityItem?.category || '-' }}</b>
          </div>
          <div>
            <span>发布范围</span>
            <b>公开</b>
          </div>
        </div>
        <p class="permission-hint detail-desc">{{ activeCommunityItem?.displayDescription || activeCommunityItem?.toolDescription || '暂无工具描述' }}</p>
      </section>

      <section class="tool-option-section">
        <div class="tool-option-title">
          <span class="tool-type-dot builtin"></span>
          入参 Schema
          <small>AI Client 看到的参数结构</small>
        </div>
        <pre class="detail-code">{{ prettyJsonText(activeCommunityItem?.inputSchema || '{}') }}</pre>
      </section>

      <section v-if="activeCommunityItem?.communityType !== 'builtin'" class="tool-option-section">
        <div class="tool-option-title">
          <span class="tool-type-dot dynamic"></span>
          API 白名单
          <small>脚本只能调用这些 configKey</small>
        </div>
        <div class="community-detail-tags">
          <span v-for="key in parseJsonArray(activeCommunityItem?.linkedRequestKeys)" :key="key">{{ key }}</span>
          <span v-if="!parseJsonArray(activeCommunityItem?.linkedRequestKeys).length">未绑定 API</span>
        </div>
      </section>

      <section v-if="activeCommunityItem?.communityType !== 'builtin'" class="tool-option-section">
        <div class="tool-option-title">
          <span class="tool-type-dot dynamic"></span>
          Groovy 脚本
          <small>tools/call 命中动态工具后执行</small>
        </div>
        <pre class="detail-code script-code">{{ activeCommunityItem?.groovyScript || '暂无脚本内容' }}</pre>
      </section>

      <section v-else class="tool-option-section">
        <div class="tool-option-title">
          <span class="tool-type-dot builtin"></span>
          实现方式
          <small>内置工具由 Java 方法注册</small>
        </div>
        <pre class="detail-code script-code">Java @Tool：{{ activeCommunityItem?.displayName || activeCommunityItem?.toolName }}</pre>
      </section>
    </template>

    <template v-else-if="activeCommunityItem?.communityKind === 'api'">
      <section class="tool-option-section">
        <div class="tool-option-title">
          <span class="tool-type-dot builtin"></span>
          基础信息
          <small>API 能力</small>
        </div>
        <div class="detail-grid">
          <div>
            <span>配置 Key</span>
            <b>{{ activeCommunityItem?.configKey || '-' }}</b>
          </div>
          <div>
            <span>接口编号</span>
            <b>{{ activeCommunityItem?.requestId || communityCode('API', activeCommunityItem?.id) }}</b>
          </div>
          <div>
            <span>请求方式</span>
            <b>{{ activeCommunityItem?.type || 'HTTP' }} / {{ activeCommunityItem?.method || 'GET' }}</b>
          </div>
          <div>
            <span>分类</span>
            <b>{{ activeCommunityItem?.category || '未分类' }}</b>
          </div>
        </div>
        <p class="permission-hint detail-desc">{{ activeCommunityItem?.description || activeCommunityItem?.name || '暂无 API 描述' }}</p>
      </section>
    </template>

    <template #footer>
      <a-button type="primary" @click="communityDetailOpen=false">关闭</a-button>
    </template>
  </a-modal>
  <a-modal v-model:open="dynamicToolDetailOpen" :title="`动态工具详情 · ${activeDynamicTool?.toolName || ''}`" width="860px" class="tool-permission-modal dynamic-tool-detail-modal">
    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot dynamic"></span>
        基础信息
        <small>{{ Number(activeDynamicTool?.enabled) === 1 ? '启用' : '禁用' }}</small>
      </div>
      <div class="detail-grid">
        <div>
          <span>工具名</span>
          <b>{{ activeDynamicTool?.toolName || '-' }}</b>
        </div>
        <div>
          <span>API 白名单</span>
          <b>{{ linkedRequestLabel(activeDynamicTool?.linkedRequestKeys) }}</b>
        </div>
        <div>
          <span>数据源白名单</span>
          <b>{{ linkedDataSourceLabel(activeDynamicTool?.linkedDataSourceIds) }}</b>
        </div>
      </div>
      <p class="permission-hint detail-desc">{{ activeDynamicTool?.toolDescription || '暂无工具描述' }}</p>
    </section>

    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot builtin"></span>
        入参 Schema
        <small>tools/list 暴露给 AI Client 的参数结构</small>
      </div>
      <pre class="detail-code">{{ activeDynamicTool?.inputSchema || '{}' }}</pre>
    </section>

    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot dynamic"></span>
        Groovy 脚本
        <small>tools/call 命中动态工具后执行</small>
      </div>
      <pre class="detail-code script-code">{{ activeDynamicTool?.groovyScript || '' }}</pre>
    </section>

    <template #footer>
      <a-button type="primary" @click="dynamicToolDetailOpen=false">关闭</a-button>
    </template>
  </a-modal>
  <a-modal v-model:open="auditDetailOpen" :title="`调用详情 · ${activeAuditLog?.toolName || ''}`" width="860px" class="tool-permission-modal dynamic-tool-detail-modal">
    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot builtin"></span>
        调用信息
        <small>{{ activeAuditLog?.status || '-' }}</small>
      </div>
      <div class="detail-grid audit-detail-grid">
        <div>
          <span>调用时间</span>
          <b>{{ activeAuditLog?.createTime || '-' }}</b>
        </div>
        <div>
          <span>调用用户</span>
          <b>{{ activeAuditLog?.userName || '-' }}</b>
        </div>
        <div>
          <span>工具名称</span>
          <b>{{ activeAuditLog?.toolName || '-' }}</b>
        </div>
        <div>
          <span>耗时</span>
          <b>{{ activeAuditLog?.durationMs ?? '-' }} ms</b>
        </div>
      </div>
    </section>

    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot dynamic"></span>
        请求参数
        <small>调用 tools/call 时传入的 arguments 摘要</small>
      </div>
      <pre class="detail-code">{{ activeAuditLog?.requestParams || '-' }}</pre>
    </section>

    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot builtin"></span>
        响应摘要
        <small>工具返回结果摘要</small>
      </div>
      <pre class="detail-code script-code">{{ activeAuditLog?.responseSummary || '-' }}</pre>
    </section>

    <section v-if="activeAuditLog?.errorMessage" class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot dynamic"></span>
        错误信息
        <small>失败调用才会出现</small>
      </div>
      <pre class="detail-code error-code">{{ activeAuditLog.errorMessage }}</pre>
    </section>

    <template #footer>
      <a-button type="primary" @click="auditDetailOpen=false">关闭</a-button>
    </template>
  </a-modal>
  <a-modal v-model:open="userRoleOpen" :title="`分配角色 · ${activeUser?.displayName || activeUser?.username || ''}`" width="720px" class="tool-permission-modal" @ok="saveUserRoles">
    <p class="permission-hint">用户通过角色获得 Tool 和 Prompt 资格。分配角色后，还需要在“角色与能力权限”里配置该角色能使用哪些能力。</p>
    <section class="tool-option-section">
      <div class="tool-option-title">
        <span class="tool-type-dot builtin"></span>
        可选角色
        <small>来自角色配置</small>
      </div>
      <a-checkbox-group v-model:value="selectedUserRoles" class="tool-option-grid">
        <a-checkbox
          v-for="role in roles"
          :key="role.roleCode"
          :value="role.roleCode"
          :disabled="role.isEnabled !== 1"
        >
          <b>{{ role.roleCode }}</b>
          <span>{{ role.roleName || '未命名角色' }}</span>
          <em v-if="role.description">{{ role.description }}</em>
        </a-checkbox>
      </a-checkbox-group>
      <a-empty v-if="!roles.length" description="暂无可分配角色" :image-style="{height:'48px'}" />
    </section>
    <template #footer>
      <a-button @click="userRoleOpen=false">取消</a-button>
      <a-button type="primary" @click="saveUserRoles">保存角色</a-button>
    </template>
  </a-modal>
  <a-modal v-model:open="toolPermissionOpen" :title="`配置工具权限 · ${activeRole?.roleName || ''}`" width="720px" class="tool-permission-modal" @ok="saveRoleTools">
    <p class="permission-hint">角色工具权限是资格上限。Token 是否实际展示和调用工具，还需要在 Token 工具选择中单独配置。</p>
    <div class="permission-bulk-actions"><a-button size="small" @click="selectAllRoleTools">全选</a-button><a-button size="small" @click="clearRoleTools">清空</a-button></div>
    <a-checkbox-group v-model:value="selectedRoleTools" class="tool-option-groups">
      <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot builtin"></span>内置工具 <small>由 Spring AI 注册</small></div><div class="tool-option-grid"><a-checkbox v-for="tool in builtinTools" :key="tool.name" :value="tool.name"><b>{{ tool.name }}</b><span>{{ tool.description }}</span></a-checkbox></div></section>
      <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot dynamic"></span>动态工具 <small>由创作空间发布</small></div><div class="tool-option-grid"><a-checkbox v-for="tool in dynamicToolOptions" :key="tool.toolName" :value="tool.toolName" :disabled="tool.enabled !== 1"><b>{{ tool.toolName }}</b><span>{{ tool.toolDescription || '暂无描述' }}</span></a-checkbox></div><a-empty v-if="!dynamicToolOptions.length" description="暂无已发布动态工具" :image-style="{height:'48px'}" /></section>
    </a-checkbox-group>
    <template #footer><a-button @click="toolPermissionOpen=false">取消</a-button><a-button type="primary" @click="saveRoleTools">保存工具权限</a-button></template>
  </a-modal>
  <a-modal v-model:open="promptPermissionOpen" :title="`配置 Prompt 权限 · ${activeRole?.roleName || ''}`" width="720px" class="tool-permission-modal" @ok="saveRolePrompts">
    <p class="permission-hint">角色 Prompt 权限是 prompts/list / prompts/get 的资格上限。Token 是否实际可见，还需要在 Token Prompt 选择中单独配置。</p>
    <div class="permission-bulk-actions"><a-button size="small" @click="selectAllRolePrompts">全选</a-button><a-button size="small" @click="clearRolePrompts">清空</a-button></div>
    <section class="tool-option-section">
      <div class="tool-option-title"><span class="tool-type-dot dynamic"></span>Prompt 模板 <small>由创作空间发布</small></div>
      <a-checkbox-group v-model:value="selectedRolePrompts" class="tool-option-grid">
        <a-checkbox v-for="prompt in promptOptions" :key="prompt.promptName" :value="prompt.promptName" :disabled="prompt.enabled !== 1 || Number(prompt.publishStatus) === 0">
          <b>{{ prompt.promptName }}</b>
          <span>{{ prompt.title || prompt.description || '暂无描述' }}</span>
          <em>{{ Number(prompt.publishStatus) === 0 ? '草稿未上线' : Number(prompt.publishStatus) === 2 ? '公开' : '不公开' }}</em>
        </a-checkbox>
      </a-checkbox-group>
      <a-empty v-if="!promptOptions.length" description="暂无 Prompt 模板" :image-style="{height:'48px'}" />
    </section>
    <template #footer><a-button @click="promptPermissionOpen=false">取消</a-button><a-button type="primary" @click="saveRolePrompts">保存 Prompt 权限</a-button></template>
  </a-modal>
  <a-modal v-model:open="resourcePermissionOpen" :title="`配置 Resource 权限 · ${activeRole?.roleName || ''}`" width="720px" class="tool-permission-modal" @ok="saveRoleResources">
    <p class="permission-hint">角色 Resource 权限是 resources/list / resources/read 的资格上限。Token 是否实际可见，还需要在 Token Resource 选择中单独配置。</p>
    <div class="permission-bulk-actions"><a-button size="small" @click="selectAllRoleResources">全选</a-button><a-button size="small" @click="clearRoleResources">清空</a-button></div>
    <section class="tool-option-section">
      <div class="tool-option-title"><span class="tool-type-dot dynamic"></span>Markdown 资源 <small>由创作空间发布</small></div>
      <a-checkbox-group v-model:value="selectedRoleResources" class="tool-option-grid">
        <a-checkbox v-for="resource in resourceOptions" :key="resource.resourceUri" :value="resource.resourceUri" :disabled="resource.enabled !== 1 || Number(resource.publishStatus) === 0">
          <b>{{ resource.name || resource.resourceUri }}</b>
          <span>{{ resource.resourceUri }}</span>
          <em>{{ Number(resource.publishStatus) === 0 ? '草稿未上线' : Number(resource.publishStatus) === 2 ? '公开' : '不公开' }}</em>
        </a-checkbox>
      </a-checkbox-group>
      <a-empty v-if="!resourceOptions.length" description="暂无 Resource" :image-style="{height:'48px'}" />
    </section>
    <template #footer><a-button @click="resourcePermissionOpen=false">取消</a-button><a-button type="primary" @click="saveRoleResources">保存 Resource 权限</a-button></template>
  </a-modal>
  <a-modal v-model:open="tokenSelectionOpen" :title="`Token 工具选择 · ${activeToken?.tokenName || ''}`" width="720px" class="tool-permission-modal" @ok="saveTokenSelections">
    <p class="permission-hint">Token 工具选择决定这把 Token 实际加载、展示和允许调用哪些工具；最终调用还会再经过角色工具权限校验。</p>
    <div class="permission-bulk-actions"><a-button size="small" @click="selectAllTokenTools">全选</a-button><a-button size="small" @click="clearTokenTools">清空</a-button></div>
    <a-checkbox-group v-model:value="selectedTokenTools" class="tool-option-groups">
      <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot builtin"></span>内置工具 <small>由 Spring AI 注册</small></div><div class="tool-option-grid"><a-checkbox v-for="tool in builtinTools" :key="tool.name" :value="tool.name"><b>{{ tool.name }}</b><span>{{ tool.description }}</span></a-checkbox></div></section>
      <section class="tool-option-section"><div class="tool-option-title"><span class="tool-type-dot dynamic"></span>动态工具 <small>由创作空间发布</small></div><div class="tool-option-grid"><a-checkbox v-for="tool in dynamicToolOptions" :key="tool.toolName" :value="tool.toolName" :disabled="tool.enabled !== 1"><b>{{ tool.toolName }}</b><span>{{ tool.toolDescription || '暂无描述' }}</span></a-checkbox></div><a-empty v-if="!dynamicToolOptions.length" description="暂无已发布动态工具" :image-style="{height:'48px'}" /></section>
    </a-checkbox-group>
    <template #footer><a-button @click="tokenSelectionOpen=false">取消</a-button><a-button type="primary" @click="saveTokenSelections">保存工具选择</a-button></template>
  </a-modal>
  <a-modal v-model:open="tokenPromptSelectionOpen" :title="`Token Prompt 选择 · ${activeToken?.tokenName || ''}`" width="720px" class="tool-permission-modal" @ok="saveTokenPromptSelections">
    <p class="permission-hint">Token Prompt 选择决定这把 Token 的 prompts/list 实际返回哪些模板；最终还会再经过角色 Prompt 权限校验。</p>
    <div class="permission-bulk-actions"><a-button size="small" @click="selectAllTokenPrompts">全选</a-button><a-button size="small" @click="clearTokenPrompts">清空</a-button></div>
    <section class="tool-option-section">
      <div class="tool-option-title"><span class="tool-type-dot dynamic"></span>Prompt 模板 <small>由创作空间发布</small></div>
      <a-checkbox-group v-model:value="selectedTokenPrompts" class="tool-option-grid">
        <a-checkbox v-for="prompt in promptOptions" :key="prompt.promptName" :value="prompt.promptName" :disabled="prompt.enabled !== 1 || Number(prompt.publishStatus) === 0">
          <b>{{ prompt.promptName }}</b>
          <span>{{ prompt.title || prompt.description || '暂无描述' }}</span>
          <em>{{ Number(prompt.publishStatus) === 0 ? '草稿未上线' : Number(prompt.publishStatus) === 2 ? '公开' : '不公开' }}</em>
        </a-checkbox>
      </a-checkbox-group>
      <a-empty v-if="!promptOptions.length" description="暂无 Prompt 模板" :image-style="{height:'48px'}" />
    </section>
    <template #footer><a-button @click="tokenPromptSelectionOpen=false">取消</a-button><a-button type="primary" @click="saveTokenPromptSelections">保存 Prompt 选择</a-button></template>
  </a-modal>
  <a-modal v-model:open="tokenResourceSelectionOpen" :title="`Token Resource 选择 · ${activeToken?.tokenName || ''}`" width="720px" class="tool-permission-modal" @ok="saveTokenResourceSelections">
    <p class="permission-hint">Token Resource 选择决定这把 Token 的 resources/list 实际返回哪些资源；最终还会再经过角色 Resource 权限校验。</p>
    <div class="permission-bulk-actions"><a-button size="small" @click="selectAllTokenResources">全选</a-button><a-button size="small" @click="clearTokenResources">清空</a-button></div>
    <section class="tool-option-section">
      <div class="tool-option-title"><span class="tool-type-dot dynamic"></span>Markdown 资源 <small>由创作空间发布</small></div>
      <a-checkbox-group v-model:value="selectedTokenResources" class="tool-option-grid">
        <a-checkbox v-for="resource in resourceOptions" :key="resource.resourceUri" :value="resource.resourceUri" :disabled="resource.enabled !== 1 || Number(resource.publishStatus) === 0">
          <b>{{ resource.name || resource.resourceUri }}</b>
          <span>{{ resource.resourceUri }}</span>
          <em>{{ Number(resource.publishStatus) === 0 ? '草稿未上线' : Number(resource.publishStatus) === 2 ? '公开' : '不公开' }}</em>
        </a-checkbox>
      </a-checkbox-group>
      <a-empty v-if="!resourceOptions.length" description="暂无 Resource" :image-style="{height:'48px'}" />
    </section>
    <template #footer><a-button @click="tokenResourceSelectionOpen=false">取消</a-button><a-button type="primary" @click="saveTokenResourceSelections">保存 Resource 选择</a-button></template>
  </a-modal>
  </a-config-provider>
  </template>
</template>
