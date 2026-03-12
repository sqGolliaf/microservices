<template>
  <div class="user-data glass-panel">
    <h2 class="form-title">Введите ваши данные</h2>
    <div class="form-group">
      <label>Имя:</label>
      <input v-model="userData.firstName" type="text" placeholder="Ваше имя">
    </div>
    <div class="form-group">
      <label>Фамилия:</label>
      <input v-model="userData.lastName" type="text" placeholder="Ваша фамилия">
    </div>
    <div class="form-group">
      <label>Почта:</label>
      <input v-model="userData.email" type="email" placeholder="Ваша почта">
    </div>
    <div class="form-group">
      <label>Пароль:</label>
      <input v-model="userData.password" type="password" placeholder="Ваш пароль">
    </div>
    <div class="form-group">
      <label>Повторите пароль:</label>
      <input v-model="userData.repPassword" type="password" placeholder="Ваш пароль">
    </div>
    <div class="btn-container">
      <router-link to="/reg" custom v-slot="{ navigate }">
        <button @click="openReg" class="submit-btn">
            Зарегистрироваться
        </button>
      </router-link>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const userData = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  repPassword: '',
})

const isUserDataValid = computed(() => {
  return userData.value.firstName.trim() !== '' &&
      userData.value.lastName.trim() !== ''
})
</script>

<style scoped>
</style>