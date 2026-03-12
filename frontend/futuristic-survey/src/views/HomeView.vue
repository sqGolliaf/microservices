<template>
  <div class="home">
    <div class="survey-container">
      <div class="survey-header">
        <h1 class="survey-title">
          <span class="title-gradient">BIBLCHECKER</span>
        </h1>
        <p class="survey-description">Ваш помощник в оформлении документации</p>
      </div>
    </div>

    <div class="question-container glass-panel">
      <h2 class="form-title">Выберите:</h2>
      <div v-for="question in questions" :key="question.id" class="question">
        <button
          @click="selectOption(currentQuestionIndex, question)"
          class="submit-btn"
        >
          {{ question.text }}
        </button>
      </div>

      <div
        v-if="inputFields.length > 0"
        :class="{ updated: dataLoaded }"
        class="input-fields"
      >
        <div class="user-data glass-panel">
          <div v-for="(inpField, index) in inputFields" :key="index" class="form-group">
            <input v-model="responses[index].text" type="text" :placeholder="inpField.text" />
          </div>

          <div v-if="serverResponseText" class="server-response">
            {{ serverResponseText }}
          </div>

          <button @click="submitRes" class="submit-btn">
            Отправить
          </button>
        </div>
      </div>

      <button
        v-if="currentQuestionIndex > 0"
        @click="prevQuestion"
        class="nav-btn"
      >
        ← Назад
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentQuestionIndex = ref(0)
const dataLoaded = ref(false)
const questions = ref([])
const questionsHistory = ref(new Map())
const selectedAnswers = ref([])
const inputFields = ref([])
const responses = ref([])
const serverResponseText = ref('')

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
    selectedAnswers.value.pop()
    questions.value = questionsHistory.value.get(currentQuestionIndex.value)
    inputFields.value = []
    responses.value = []
    serverResponseText.value = ''
  }
}

const nextQuestion = () => {
  currentQuestionIndex.value++
}

const fetchQuestionsMain = async () => {
  try {
    const response = await fetch('http://localhost:8080/')
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const data = await response.json()
    if (Array.isArray(data) && data.length > 0) {
      questions.value = data.map(item => ({
        id: item.id,
        text: item.text,
        options: item.storage?.options || null,
        attribute: item.attribute || 'false'
      }))
      saveToCache()
    }
  } catch (error) {
    console.error('Ошибка при загрузке вопросов:', error)
    alert('Не удалось загрузить вопрос')
  }
}

const fetchQuestion = async (id, att) => {
  if (att === 'false') {
    await fetchQuestionsToIs(id)
  } else if (att === 'true') {
    await fetchQuestionsToPath(id)
  }
}

const fetchQuestionsToIs = async (id) => {
  try {
    const response = await fetch(`http://localhost:8080/to/${id}`)
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const data = await response.json()
    if (Array.isArray(data) && data.length > 0) {
      questionsHistory.value.set(currentQuestionIndex.value, questions.value)
      questions.value = data.map(item => ({
        id: item.id,
        text: item.text,
        options: item.storage?.options || null,
        attribute: item.attribute || 'false'
      }))
      saveToCache()
      nextQuestion()
    }
  } catch (error) {
    console.error('Ошибка при загрузке вопросов:', error)
    alert('Не удалось загрузить вопрос')
  }
}

const fetchQuestionsToPath = async (id) => {
  try {
    dataLoaded.value = false
    const response = await fetch(`http://localhost:8080/path/${id}`)
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const data = await response.json()
    if (Array.isArray(data) && data.length > 0) {
      questionsHistory.value.set(currentQuestionIndex.value, questions.value)
      questions.value = []
      inputFields.value = data.map(item => ({ id: item.id, text: item.text }))
      responses.value = data.map(item => ({ id: item.id, text: '' }))
      dataLoaded.value = true
      saveToCache()
      nextQuestion()
    }
  } catch (error) {
    console.error('Ошибка при загрузке вопросов:', error)
    alert('Не удалось загрузить вопрос')
  }
}

const selectOption = (currentQuestionIndex, question) => {
  const newAnswers = selectedAnswers.value.filter(a => a.questionId !== currentQuestionIndex)
  newAnswers.push({
    currentQuestionIndex,
    id: question.id,
    text: question.text,
    timestamp: new Date().toISOString()
  })
  selectedAnswers.value = newAnswers
  fetchQuestion(question.id, question.attribute)
}

const submitRes = async () => {
  try {
    const response = await fetch('http://localhost:8080/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(responses.value)
    })
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const result = await response.text()
    serverResponseText.value = result
  } catch (error) {
    console.error('Ошибка при отправке данных:', error)
    serverResponseText.value = 'Ошибка при отправке ответа.'
  }
}

const saveToCache = () => {
  localStorage.setItem('surveyData', JSON.stringify({
    questions: questions.value,
    answers: selectedAnswers.value
  }))
}

onMounted(() => {
  fetchQuestionsMain()
  const savedAnswers = localStorage.getItem('surveyAnswers')
  if (savedAnswers) {
    const data = JSON.parse(savedAnswers)
    selectedAnswers.value = data.answers || []
    if (data.questions && data.questions.length > 0) {
      questions.value = data.questions
    }
  }
})
</script>

<style scoped>
.question-container {
  margin-top: 10%;
}
.updated {
  transform: scale(1.05);
}
.input-field {
  margin-top: 10px;
}
.server-response {
  margin-bottom: 10px;
  font-weight: bold;
  color: #4caf50;
}
</style>
