export const environment = {
  production: false,

  msal: {
    // App Registration del FRONTEND Angular
    clientId: '2b2e50e2-6672-4faa-9d4f-c62bc25836c8',

    // Directory (tenant) ID del tenant utilizado en Microsoft Entra ID
    tenantId: 'ea55240a-61c7-4ab9-a10a-04783c525811',

    redirectUri: 'http://localhost:4200',

    // Scope expuesto por la App Registration de la API
    apiScope:
      'api://227cb7f1-be73-470a-ae95-81aab39cb9f1/Pedidos.Read'
  },

  // Backend Spring Boot de la Sesión 3
  apiBaseUrl: 'http://localhost:8080'
};
