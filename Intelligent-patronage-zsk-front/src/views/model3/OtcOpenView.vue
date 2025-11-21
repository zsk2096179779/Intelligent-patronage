<template>
  <div class="otc-page">
    <!-- 顶部标题 -->
    <header class="otc-header">
      <h2>开通场外账户（OTC）</h2>
      <p class="sub">
        为了完成智能投顾组合的签约与交易，您需要先开通场外资金账户。
        请确认您的身份信息、联系方式和银行卡信息真实有效。
      </p>
    </header>

    <!-- 状态卡片 -->
    <el-card class="status-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>当前开通状态</span>
          <el-tag
            v-if="otcStatus"
            :type="otcStatus.opened ? 'success' : 'info'"
          >
            {{ otcStatus.opened ? '已开通' : '未开通' }}
          </el-tag>
        </div>
      </template>

      <div v-if="statusLoading" class="loading-area">
        <el-skeleton :rows="2" animated />
      </div>

      <div v-else>
        <!-- 已开通状态展示 -->
        <div v-if="otcStatus?.opened" class="status-info">
          <p>
            <strong>OTC 账号：</strong>{{ otcStatus.accountNo || '—' }}
          </p>
          <p>
            <strong>开通时间：</strong>{{ formatDateTime(otcStatus.openDate) }}
          </p>
          <el-alert
            type="success"
            show-icon
            title="您已经开通场外账户，可以直接返回组合页面进行签约操作。"
            class="mt-2"
          />
          <div class="status-actions">
            <el-button type="primary" @click="goBack">
              返回组合列表
            </el-button>
          </div>
        </div>

        <!-- 未开通时提示 -->
        <div v-else class="status-info">
          <el-alert
            type="warning"
            show-icon
            title="当前尚未开通场外账户"
            description="请根据下方表单填写真实有效的身份和银行卡信息，并勾选相关协议后提交申请。"
          />
        </div>
      </div>
    </el-card>

    <!-- 开通表单（仅未开通时显示） -->
    <el-card
      v-if="!otcStatus?.opened"
      class="form-card"
      shadow="never"
    >
      <template #header>
        <div class="card-header">
          <span>开户信息填写</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="otc-form"
      >
        <el-form-item label="客户姓名" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="请输入您的真实姓名"
          />
        </el-form-item>

        <el-form-item label="证件号码" prop="idCardNo">
          <el-input
            v-model="form.idCardNo"
            placeholder="请输入您的身份证号码"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="mobile">
          <el-input
            v-model="form.mobile"
            placeholder="请输入预留手机号"
          />
        </el-form-item>

        <el-form-item label="银行卡号" prop="bankCardNo">
          <el-input
            v-model="form.bankCardNo"
            placeholder="请输入用于扣款的银行卡号"
          />
        </el-form-item>

        <el-form-item label="开户行" prop="bankName">
          <el-input
            v-model="form.bankName"
            placeholder="如：中国银行北京某某支行"
          />
        </el-form-item>

        <el-form-item label="短信验证码" prop="smsCode">
          <div class="sms-row">
            <el-input
              v-model="form.smsCode"
              placeholder="请输入短信验证码"
              maxlength="6"
            />
            <el-button
              class="sms-btn"
              :disabled="smsSending || smsCountDown > 0"
              @click="sendSmsCode"
            >
              <span v-if="smsCountDown > 0">
                {{ smsCountDown }} 秒后重发
              </span>
              <span v-else>获取验证码</span>
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="agreed">
            我已阅读并同意
            <a href="javascript:void(0)" @click="showAgreement">
              《场外账户业务协议》
            </a>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button @click="goBack">取消</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!agreed"
            @click="handleSubmit"
          >
            提交开通申请
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 协议弹窗（简单版占位） -->
    <el-dialog
      v-model="agreementVisible"
      title="场外账户业务协议（示意）"
      width="600px"
    >
      <p class="agreement-text">
        此处为协议内容占位。实际项目中可由后端下发 PDF 或富文本，
        或直接在前端维护一份正式的业务协议文本。
      </p>
      <template #footer>
        <el-button @click="agreementVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getApiUrl, API_CONFIG } from '../../config/api.ts'
// 如果有 authStore，需要的话可以引入
// import { useAuthStore } from '@/stores/auth'

// 接口返回的 OTC 状态类型
interface OtcStatus {
  opened: boolean
  accountNo?: string | null
  openDate?: string | null
}

// 开户表单类型
interface OtcOpenForm {
  realName: string
  idCardNo: string
  mobile: string
  bankCardNo: string
  bankName: string
  smsCode: string
}

const router = useRouter()
// const authStore = useAuthStore()

const statusLoading = ref(false)
const otcStatus = ref<OtcStatus | null>(null)

const formRef = ref<FormInstance>()
const form = reactive<OtcOpenForm>({
  realName: '',
  idCardNo: '',
  mobile: '',
  bankCardNo: '',
  bankName: '',
  smsCode: ''
})

const submitting = ref(false)
const agreed = ref(false)

const smsSending = ref(false)
const smsCountDown = ref(0)
let smsTimer: number | null = null

const agreementVisible = ref(false)

// 表单校验规则
const rules: FormRules<OtcOpenForm> = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCardNo: [{ required: true, message: '请输入身份证号码', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1\d{10}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ],
  bankCardNo: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    {
      min: 8,
      message: '银行卡号长度不正确',
      trigger: 'blur'
    }
  ],
  bankName: [{ required: true, message: '请输入开户行名称', trigger: 'blur' }],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    {
      pattern: /^\d{4,6}$/,
      message: '验证码格式不正确',
      trigger: 'blur'
    }
  ]
}

// 获取当前 OTC 状态 & 可选预填画像信息
const fetchOtcStatus = async () => {
  statusLoading.value = true
  try {
    const res = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.OTC_STATUS))
    const raw = res.data
    const data = raw.data ?? raw
    otcStatus.value = data as OtcStatus

    // 如果你有 INVESTOR_PROFILE 接口，也可以在这里预填一下表单
    const profileRes = await axios.get(getApiUrl(API_CONFIG.ENDPOINTS.INVESTOR_PROFILE))
    const profile = (profileRes.data?.data ?? {}) as any
    if (profile.realName) form.realName = profile.realName
    if (profile.mobile) form.mobile = profile.mobile
    if (profile.bankName) form.bankName = profile.bankName
    if (profile.bankCardNo) form.bankCardNo = profile.bankCardNo
  } catch (e) {
    console.error('获取OTC状态失败', e)
    ElMessage.error('获取 OTC 状态失败，请稍后重试')
  } finally {
    statusLoading.value = false
  }
}

// 模拟发送短信验证码（占位逻辑）
const sendSmsCode = async () => {
  if (!form.mobile) {
    ElMessage.warning('请先填写手机号')
    return
  }
  // 这里可以后续接后端真实短信接口，现在先本地模拟
  smsSending.value = true
  try {
    ElMessage.success('验证码已发送（模拟），请输入 6 位数字')
    smsCountDown.value = 60
    if (smsTimer) {
      window.clearInterval(smsTimer)
    }
    smsTimer = window.setInterval(() => {
      smsCountDown.value -= 1
      if (smsCountDown.value <= 0 && smsTimer) {
        window.clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } finally {
    smsSending.value = false
  }
}

// 提交开户申请
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意相关协议')
    return
  }

  submitting.value = true
  try {
    await axios.post(getApiUrl(API_CONFIG.ENDPOINTS.OTC_OPEN), {
      realName: form.realName,
      idCardNo: form.idCardNo,
      mobile: form.mobile,
      bankCardNo: form.bankCardNo,
      bankName: form.bankName,
      smsCode: form.smsCode
    })

    ElMessage.success('OTC 账户开通成功')
    // 开通成功后刷新状态
    await fetchOtcStatus()
  } catch (e) {
    console.error('开通OTC失败', e)
    if (axios.isAxiosError(e)) {
      ElMessage.error(e.response?.data?.message || '开通 OTC 失败，请稍后重试')
    } else {
      ElMessage.error('开通 OTC 失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  // 你可以根据实际路由改成组合列表/签约页面的路径
  router.push('/market')
}

const showAgreement = () => {
  agreementVisible.value = true
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '—'
  try {
    const normalized = value.replace(' ', 'T')
    const d = new Date(normalized)
    if (Number.isNaN(d.getTime())) return value
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${hh}:${mm}`
  } catch {
    return value || '—'
  }
}

onMounted(() => {
  fetchOtcStatus()
})
</script>

<style scoped>
.otc-page {
  max-width: 900px;
  margin: 24px auto;
  padding: 0 16px 32px;
}

.otc-header {
  margin-bottom: 16px;
}

.otc-header h2 {
  margin: 0 0 8px;
}

.otc-header .sub {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.status-card,
.form-card {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.loading-area {
  padding: 12px 0;
}

.status-info p {
  margin: 4px 0;
}

.status-actions {
  margin-top: 12px;
}

.mt-2 {
  margin-top: 12px;
}

.otc-form {
  max-width: 600px;
}

.sms-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.sms-btn {
  white-space: nowrap;
}

.agreement-text {
  font-size: 13px;
  line-height: 1.6;
  color: #555;
}
</style>
