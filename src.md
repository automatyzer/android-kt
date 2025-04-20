app/
├── build.gradle
├── src/
├── main/
├── java/com/businessprospector/
│   ├── MainActivity.kt
│   ├── BusinessProspectorApplication.kt
│   ├── di/
│   │   └── AppModule.kt
│   ├── data/
│   │   ├── repository/
│   │   │   ├── ContactRepository.kt
│   │   │   ├── SearchRepository.kt
│   │   │   ├── CommunicationRepository.kt
│   │   │   └── AnalyticsRepository.kt
│   │   ├── local/
│   │   │   ├── dao/
│   │   │   │   ├── ContactDao.kt
│   │   │   │   ├── MessageDao.kt
│   │   │   │   ├── SequenceDao.kt
│   │   │   │   └── AnalyticsDao.kt
│   │   │   ├── entity/
│   │   │   │   ├── ContactEntity.kt
│   │   │   │   ├── MessageEntity.kt
│   │   │   │   ├── SequenceEntity.kt
│   │   │   │   └── AnalyticsEntity.kt
│   │   │   └── AppDatabase.kt
│   │   ├── remote/
│   │   │   ├── api/
│   │   │   │   ├── GoogleSearchApi.kt
│   │   │   │   ├── LlmApi.kt
│   │   │   │   └── EmailSmsApi.kt
│   │   │   ├── dto/
│   │   │   │   ├── GoogleSearchResponse.kt
│   │   │   │   ├── LlmResponse.kt
│   │   │   │   └── CommunicationResponse.kt
│   │   │   └── NetworkModule.kt
│   │   └── model/
│   │       ├── Contact.kt
│   │       ├── Message.kt
│   │       ├── Sequence.kt
│   │       └── Analytics.kt
│   ├── domain/
│   │   ├── usecase/
│   │   │   ├── search/
│   │   │   │   ├── SearchContactsUseCase.kt
│   │   │   │   ├── ParseSearchResultsUseCase.kt
│   │   │   │   └── FilterContactsUseCase.kt
│   │   │   ├── analysis/
│   │   │   │   ├── EnrichContactDataUseCase.kt
│   │   │   │   ├── GenerateMessageUseCase.kt
│   │   │   │   └── CategorizeContactsUseCase.kt
│   │   │   ├── communication/
│   │   │   │   ├── SendEmailUseCase.kt
│   │   │   │   ├── SendSmsUseCase.kt
│   │   │   │   ├── MakeCallUseCase.kt
│   │   │   │   └── ExecuteSequenceUseCase.kt
│   │   │   └── analytics/
│   │   │       ├── TrackResponseUseCase.kt
│   │   │       ├── GenerateReportUseCase.kt
│   │   │       └── OptimizeStrategyUseCase.kt
│   │   └── service/
│   │       ├── WorkManagerService.kt
│   │       ├── NotificationService.kt
│   │       └── EncryptionService.kt
│   ├── ui/
│   │   ├── theme/
│   │   │   └── Theme.kt
│   │   ├── common/
│   │   │   ├── components/
│   │   │   └── extensions/
│   │   ├── search/
│   │   │   ├── SearchScreen.kt
│   │   │   ├── SearchViewModel.kt
│   │   │   └── components/
│   │   ├── contacts/
│   │   │   ├── ContactsScreen.kt
│   │   │   ├── ContactsViewModel.kt
│   │   │   ├── ContactDetailScreen.kt
│   │   │   ├── ContactDetailViewModel.kt
│   │   │   └── components/
│   │   ├── messages/
│   │   │   ├── MessagesScreen.kt
│   │   │   ├── MessagesViewModel.kt
│   │   │   ├── TemplateEditorScreen.kt
│   │   │   ├── TemplateEditorViewModel.kt
│   │   │   └── components/
│   │   ├── sequences/
│   │   │   ├── SequencesScreen.kt
│   │   │   ├── SequencesViewModel.kt
│   │   │   ├── SequenceEditorScreen.kt
│   │   │   ├── SequenceEditorViewModel.kt
│   │   │   └── components/
│   │   ├── analytics/
│   │   │   ├── AnalyticsScreen.kt
│   │   │   ├── AnalyticsViewModel.kt
│   │   │   ├── ReportScreen.kt
│   │   │   ├── ReportViewModel.kt
│   │   │   └── components/
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       ├── SettingsViewModel.kt
│   │       ├── ApiConfigScreen.kt
│   │       ├── ApiConfigViewModel.kt
│   │       └── components/
│   └── worker/
│       ├── SearchWorker.kt
│       ├── AnalysisWorker.kt
│       ├── CommunicationWorker.kt
│       └── SyncWorker.kt
└── res/
├── layout/
├── drawable/
├── values/
└── navigation/
└── nav_graph.xml