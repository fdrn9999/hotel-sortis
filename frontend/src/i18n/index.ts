import { createI18n } from 'vue-i18n'
import ko from './locales/ko.json'
import en from './locales/en.json'
import ja from './locales/ja.json'
import zh from './locales/zh.json'

export type SupportedLocale = 'ko' | 'en' | 'ja' | 'zh'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  fallbackLocale: 'en',
  messages: {
    ko,
    en,
    ja,
    zh
  }
})

export default i18n
