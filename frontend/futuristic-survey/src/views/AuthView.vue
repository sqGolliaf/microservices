<template>
  <div class="user-data glass-panel">
    <h2 class="form-title">Введите ваши данные</h2>
    <div class="form-group">
      <label>Логин:</label>
      <input v-model="userData.firstName" type="text" placeholder="Ваш логин">
    </div>
    <div class="form-group">
      <label>Пароль:</label>
      <input v-model="userData.lastName" type="text" placeholder="Ваш пароль">
    </div>
        
    <div class="btn-container">
      <router-link to="/reg" custom v-slot="{ navigate }">
        <button @click="openReg" class="submit-btn">
          Авторизоваться
        </button>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'


const userData = ref({
  login: '',
  password: '',
})
</script>

<style scoped>
</style>