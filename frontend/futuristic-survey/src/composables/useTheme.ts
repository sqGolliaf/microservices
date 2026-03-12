import { ref, onMounted } from 'vue'

export default function useTheme() {
    const currentTheme = ref<'light' | 'dark'>('dark')

    const setTheme = (theme: 'light' | 'dark') => {
        currentTheme.value = theme
        document.documentElement.setAttribute('data-theme', theme)
        localStorage.setItem('theme', theme)
    }

    const toggleTheme = () => {
        setTheme(currentTheme.value === 'light' ? 'dark' : 'light')
    }

    onMounted(() => {
        const savedTheme = localStorage.getItem('theme') as 'light' | 'dark'
        if (savedTheme) {
            setTheme(savedTheme)
        }
    })

    return {
        currentTheme,
        setTheme,
        toggleTheme
    }
}