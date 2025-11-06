import {defineStore} from "pinia";

export const useDocumentStore = defineStore('document', {
    state: () => ({
        documents: [],
    }),
    actions: {
        reset() {
            this.documents = []
        }
    }
})
