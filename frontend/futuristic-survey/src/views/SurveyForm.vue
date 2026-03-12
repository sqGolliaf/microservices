<template>
  <div v-if="currentStep === 1" class="user-data glass-panel">
    <h2>Введите ваши данные</h2>
    <div class="form-group">
      <label>Имя:</label>
      <input v-model="userData.firstName" type="text" placeholder="Ваше имя">
    </div>
    <div class="form-group">
      <label>Фамилия:</label>
      <input v-model="userData.lastName" type="text" placeholder="Ваша фамилия">
    </div>
    <div class="form-group">
      <label>Email:</label>
      <input v-model="userData.email" type="email" placeholder="Ваш email">
    </div>
    <button @click="fetchQuestionsMain" class="submit-btn" >
      Авторизация
    </button>
    <button @click="fetchQuestionsMain" class="submit-btn" >
      Регистрация
    </button>
    <button @click="fetchQuestionsMain" class="submit-btn" >
      Старт
    </button>
  </div>

  <div v-else-if="currentStep === 2" class="question-container glass-panel">
    <h2>Выберите:</h2>
    <div v-for="question in questions" class="question">
        <button
            :key="question.id"
            @click="selectOption(currentQuestionIndex, question)"
            class="submit-btn"
        >
          {{ question.text }}
        </button>
    </div>
    <button v-if="currentQuestionIndex > 0" @click="prevQuestion()" class="nav-btn">
      ← Назад
    </button>
  </div>
</template>

<script setup>

import { ref, onMounted, computed } from 'vue'

const currentStep = ref(1)
const currentQuestionIndex = ref(0)

const questions = ref([])
const questionsHistory = ref(new Map())
const selectedAnswers = ref([])

const userData = ref({
  firstName: '',
  lastName: '',
  email: ''
})


const isUserDataValid = computed(() => {
  return userData.value.firstName.trim() !== '' &&
      userData.value.lastName.trim() !== '' &&
      /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userData.value.email)
})

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
    selectedAnswers.value.pop()
    questions.value = questionsHistory.value.get(currentQuestionIndex)
  }
}

const nextQuestion = () => {
    currentQuestionIndex.value++
}

onMounted(() => {
  const savedAnswers = localStorage.getItem('surveyAnswers')
  if (savedAnswers) {
    const data = JSON.parse(savedAnswers)
    userData.value = data.userData || userData.value
    selectedAnswers.value = data.answers || []

    if (data.questions && data.questions.length > 0) {
      questions.value = data.questions
      currentStep.value = 2
    }
  }
})

const allQuestionsAnswered = computed(() => {
  return questions.value.every(q =>
      selectedAnswers.value.some(a => a.questionId === q.id)
  )
})

const fetchQuestionsMain = async () => {
  try {
    const url = 'http://localhost:8080/'
    const response = await fetch(url)

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log('Ответ сервера:', data)

    if (Array.isArray(data) && data.length > 0) {
      questions.value = data.map(item => ({
        id: item.id,
        text: item.text,
        options: item.storage?.options || null
      }))

      currentStep.value = 2
      saveToCache()
    } else {
      currentStep.value = 3
    }
  } catch (error) {
    console.error('Ошибка при загрузке вопросов:', error)
    alert('Не удалось загрузить вопрос')
  }
}

const fetchQuestionsTo = async (id) => {
  try {
    const url = `http://localhost:8080/to/${id}`
    const response = await fetch(url)

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log('Ответ сервера:', data)

    if (Array.isArray(data) && data.length > 0) {
      questionsHistory.value.set(currentQuestionIndex, questions.value)
      questionsHistory.value.forEach((key, value) => {
        console.log('key:', key, 'value:', value)
      })
      questions.value = data.map(item => ({
        id: item.id,
        text: item.text,
        options: item.storage?.options || null
      }))

      currentStep.value = 2
      saveToCache()
      nextQuestion()
    } else {
      currentStep.value = 3
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
  fetchQuestionsTo(question.id)
}

const completeSurvey = async () => {
  const payload = {
    user: userData.value,
    answers: selectedAnswers.value.map(answer => ({
      questionId: answer.questionId,
      optionId: answer.id,
      timestamp: answer.timestamp
    })),
    completedAt: new Date().toISOString()
  }

  try {
    // const response = await fetch('http://localhost:8080/saveAnswer', {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json'
    //   },
    //   body: JSON.stringify(payload)
    // })
    //
    // if (!response.ok) throw new Error('Ошибка сохранения ответов')
    //
    // const result = await response.json()
    // console.log('Ответ сервера:', result)
    console.log('Будет отправлена')

  } catch (error) {
    console.error('Ошибка:', error)
    alert('Не удалось отправить ответы. Пожалуйста, попробуйте позже.')
  } finally {
    resetSurvey()
    currentStep.value = 1
  }
}

const resetSurvey = () => {
  currentStep.value = 1
  userData.value = { firstName: '', lastName: '', email: '' }
  questions.value = []
  selectedAnswers.value = []
  currentQuestionIndex.value = 0
  localStorage.removeItem('surveyAnswers')
}

const saveToCache = () => {
  localStorage.setItem('surveyData', JSON.stringify({
    userData: userData.value,
    questions: questions.value,
    answers: selectedAnswers.value
  }))
}

</script>

<style scoped>
.user-data {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px;
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  background: var(--glass);
  color: var(--text);
}

.question-main h3 {
  font-size: 1.5rem;
  margin-bottom: 30px;
  color: var(--primary);
  text-align: center;
}

.nav-btn {
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  padding: 8px 15px;
  font-size: 1rem;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn {
  margin-top: 20px;
  padding: 12px 30px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(110, 69, 226, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.nav-btn {
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  padding: 8px 15px;
  font-size: 1rem;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.completion-screen {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px;
  text-align: center;
  width: 100%;
}

.summary {
  margin: 30px 0;
  text-align: left;
  background: var(--glass);
  padding: 25px;
  border-radius: 10px;
}

.summary h3 {
  color: var(--accent);
  margin-bottom: 20px;
  text-align: center;
}

.summary ul {
  list-style-type: none;
  padding: 0;
}

.summary li {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--glass-border);
}

@media (max-width: 768px) {
  .options-grid {
    grid-template-columns: 1fr;
  }

  .question-main {
    padding: 25px;
  }

  .user-data,
  .completion-screen {
    padding: 30px;
  }
}
</style>