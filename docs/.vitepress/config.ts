import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'LedgerKit',
  description: 'Kotlin JVM finance domain framework',
  lang: 'en',
  base: '/',
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' }]
  ],
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'Concepts', link: '/concepts/money' },
      { text: 'Modules', link: '/modules/analytics' },
      { text: 'Guides', link: '/guides/import-export' },
      { text: 'GitHub', link: 'https://github.com/ByteCraft-Co/ledgerkit-core' }
    ],
    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Overview', link: '/getting-started' }
        ]
      },
      {
        text: 'Concepts',
        items: [
          { text: 'Money', link: '/concepts/money' },
          { text: 'Transaction', link: '/concepts/transaction' },
          { text: 'Category', link: '/concepts/category' },
          { text: 'Budget', link: '/concepts/budget' },
          { text: 'Recurrence', link: '/concepts/recurrence' },
          { text: 'Tags & IDs', link: '/concepts/tags-and-ids' }
        ]
      },
      {
        text: 'Modules',
        items: [
          { text: 'Analytics', link: '/modules/analytics' },
          { text: 'Rules', link: '/modules/rules' },
          { text: 'Storage', link: '/modules/storage' },
          { text: 'Import/Export', link: '/modules/import-export' }
        ]
      },
      {
        text: 'Guides',
        items: [
          { text: 'Import & Export', link: '/guides/import-export' }
        ]
      },
      {
        text: 'Cookbook',
        items: [
          { text: 'Common Recipes', link: '/cookbook/common-recipes' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/ByteCraft-Co/ledgerkit-core' }
    ]
  }
})
