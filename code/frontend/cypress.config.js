import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    baseUrl: "http://localhost:5173",
    // eslint-disable-next-line no-unused-vars
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
    reporter: "junit",
    reporterOptions: {
      mochaFile: "cypress/reports/junit.xml",
    },
  },
});
