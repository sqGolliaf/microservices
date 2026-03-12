import { createApp } from 'vue'
import App from './App.vue'
import AOS from 'aos'
import 'aos/dist/aos.css'
import router from './router'

const app = createApp(App)

app.AOS = AOS.init({
    duration: 800,
    easing: 'ease-in-out',
    once: true
})

app.use(router)
app.mount('#app')