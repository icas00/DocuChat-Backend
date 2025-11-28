# Implementation Plan - Embeddable AI Widget Platform

## Objective
Pivot the existing multi-tenant SaaS backend to a single-tenant "Embeddable AI Widget Platform".

## Task 1: Backend Refactoring (Spring Boot)

### 1.1 Simplify Entity Model
*   **Create `Client` Entity**:
    *   Fields: `Long id`, `String name`, `String apiKey` (unique), `String websiteUrl`.
    *   Location: `com.aiassistant.model.Client`
*   **Update `FaqDoc` Entity**:
    *   Replace `Org org` with `Client client`.
*   **Update `Conversation` Entity**:
    *   Replace `Org org` with `Client client`.
*   **Delete Obsolete Entities**:
    *   `Org.java`, `User.java` (if not needed for admin, but maybe keep for now or delete if strictly single tenant/no login? User said "Remove complex Organization hierarchy", implying `Org` is gone. `User` might be needed for admin panel, but for now I will focus on Widget. I will delete `Org` as requested).
    *   `TelephonySession.java`.

### 1.2 Update Repositories
*   **Create `ClientRepository`**: `findByApiKey(String apiKey)`.
*   **Update `FaqDocRepository`**: `findByClientId(Long clientId)`.
*   **Update `ConversationRepository`**: `findByClientId(Long clientId)`.
*   **Delete**: `OrgRepository`.

### 1.3 Service Layer Updates
*   **Create `ClientService`**: To handle Client retrieval.
*   **Update `EmbeddingService`**: Use `clientId` instead of `orgId`.
*   **Update `ChatService`**:
    *   Inject `ClientService` (or Repository).
    *   Use `Client` for context.
    *   Remove `OrgService` dependency.
*   **Delete**: `OrgService`, `VoiceService`.

### 1.4 API Layer (Widget Controller)
*   **Create `WidgetController`**:
    *   `POST /api/widget/chat`
    *   Request Body: `WidgetChatRequest` (`apiKey`, `message`, `conversationId`).
    *   Logic: Validate API Key -> `chatService.chat(...)`.
*   **Update `SecurityConfig`**:
    *   Allow `POST /api/widget/**` from `*`.

## Task 2: Widget Loader (JavaScript)

### 2.1 Create `loader.js`
*   Location: `backend/src/main/resources/static/loader.js`
*   Features:
    *   Auto-execute on load.
    *   Read `data-api-key` from current script tag.
    *   Create container div.
    *   Attach Shadow DOM.
    *   Inject Styles (scoped).
    *   Render Chat UI (Simple Fetch + DOM manipulation for MVP).

## Task 3: Demo Page

### 3.1 Create `demo.html`
*   Location: `backend/demo.html` (or root).
*   Content: Mock "Dental Clinic" site.
*   Script Tag: `<script src="http://localhost:8080/loader.js" data-api-key="TEST_KEY"></script>`.

## Execution Steps
1.  **Refactor Entities**: Create `Client`, update `FaqDoc`, `Conversation`. Delete `Org`.
2.  **Refactor Repositories**: Create/Update repositories.
3.  **Refactor Services**: Update `EmbeddingService`, `ChatService`. Delete `VoiceService`, `OrgService`.
4.  **Create Controller**: `WidgetController`.
5.  **Update Security**: `SecurityConfig`.
6.  **Create Loader**: `loader.js`.
7.  **Create Demo**: `demo.html`.
