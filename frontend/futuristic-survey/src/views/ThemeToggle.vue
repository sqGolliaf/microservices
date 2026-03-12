<template>
  <div class="theme-toggle">
    <input
        type="checkbox"
        id="theme-switch"
        class="toggle-input"
        :checked="currentTheme === 'light'"
        @change="toggleTheme"
    >
    <label for="theme-switch" class="toggle-label">
      <span class="toggle-handle"></span>
      <span class="icons">
        <svg class="sun" viewBox="0 0 24 24"><path d="M12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18M20,15.31L23.31,12L20,8.69V4H15.31L12,0.69L8.69,4H4V8.69L0.69,12L4,15.31V20H8.69L12,23.31L15.31,20H20V15.31Z"></path></svg>
        <svg class="moon" viewBox="0 0 24 24"><path d="M12,18C11.11,18 10.26,17.8 9.5,17.45C11.56,16.5 13,14.42 13,12C13,9.58 11.56,7.5 9.5,6.55C10.26,6.2 11.11,6 12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18M20,8.69V4H15.31L12,0.69L8.69,4H4V8.69L0.69,12L4,15.31V20H8.69L12,23.31L15.31,20H20V15.31L23.31,12L20,8.69Z"></path></svg>
      </span>
    </label>
  </div>
</template>

<script setup>
import useTheme from '../composables/useTheme'

const { currentTheme, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
}

.toggle-input {
  display: none;
}

.toggle-label {
  display: flex;
  align-items: center;
  width: 60px;
  height: 30px;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 50px;
  padding: 5px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.toggle-handle {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border-radius: 50%;
  position: absolute;
  left: 5px;
  transition: all 0.3s ease;
  z-index: 1;
}

.toggle-input:checked + .toggle-label .toggle-handle {
  transform: translateX(30px);
}

.icons {
  display: flex;
  width: 100%;
  justify-content: space-between;
  position: relative;
}

.icons svg {
  width: 16px;
  height: 16px;
  fill: var(--text);
  opacity: 0.7;
  transition: all 0.3s ease;
}

.sun {
  opacity: 0 !important;
}

[data-theme="light"] .sun {
  opacity: 1 !important;
}

[data-theme="light"] .moon {
  opacity: 0 !important;
}
</style>