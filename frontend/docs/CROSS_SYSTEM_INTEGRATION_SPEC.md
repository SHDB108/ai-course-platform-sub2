# Cross-Subsystem Integration Technical Specification

## Document Information
- **Document Title**: AI Course Platform - Distributed Service Architecture & Integration Strategy
- **Version**: 1.0
- **Last Updated**: 2025-12-04
- **Target Audience**: Technical Stakeholders, System Architects, Project Reviewers

---

## 1. Executive Summary

The AI Course Platform implements a **distributed service architecture** based on micro-frontend principles, enabling independent deployment and scaling of functionally isolated subsystems while maintaining a unified user experience through strategic integration points.

---

## 2. System Architecture Overview

### 2.1 Subsystem Composition

The platform comprises two independently developed and deployed service modules:

| Subsystem | Primary Responsibilities | Deployment Endpoint |
|-----------|-------------------------|---------------------|
| **Student Learning System (Subsystem A)** | Course content delivery, knowledge graph visualization, personalized learning path recommendation engine, study progress analytics | `http://localhost:5173/` |
| **AI Exam System (Subsystem B)** | Intelligent assessment generation, adaptive testing algorithms, real-time proctoring, performance evaluation | `http://172.22.126.152:5173/` |

### 2.2 Architectural Rationale

This **Service Isolation Strategy** ensures:

- **Resilience Under Load**: High-concurrency exam scenarios (e.g., simultaneous student assessments) are isolated from core learning services, preventing cascade failures
- **Independent Scalability**: Each subsystem can be horizontally scaled based on specific resource demands
- **Autonomous Deployment Cycles**: Teams can release updates independently without coordinating deployment windows
- **Fault Containment**: Service disruptions are confined to individual subsystem boundaries

---

## 3. Cross-Subsystem Integration Mechanism

### 3.1 Integration Pattern: Lightweight Identity Context Propagation

To deliver a seamless Single Sign-On (SSO) experience without implementing centralized authentication infrastructure, the platform employs **client-side identity context propagation** through URL-based parameter passing.

#### 3.1.1 Technical Implementation

**Identity Retrieval:**
```typescript
// Source: src/views/course/index.vue:405-409
const userStore = useUserStore()  // Pinia state management
const studentId = userStore.userId || 1  // Retrieve authenticated user ID
```

**Context Transmission:**
```typescript
const examSystemUrl = `http://172.22.126.152:5173/student/exam?studentId=${studentId}`
window.open(examSystemUrl, '_blank')  // New browser context
```

**Data Contract:**
```
Query Parameter: studentId
Type: Integer (required)
Fallback: Default to 1 if userStore.userId is null
Transport: HTTP GET with client-side redirect
```

### 3.2 Integration Entry Point

**User Interface Location:**
- **View Component**: `src/views/course/index.vue:57-83`
- **Panel**: AI Smart Recommendations Card Header
- **Trigger Element**: Action button labeled "进入AI考试系统" (Enter AI Exam System)

**Visual Design Specifications:**
```vue
<el-button type="warning" @click="navigateToExamSystem">
  <el-icon><Monitor /></el-icon>
  进入AI考试系统
</el-button>
```

- **Button Variant**: `warning` (amber/orange visual treatment per Element Plus design system)
- **Icon**: `Monitor` (symbolizing system transition)
- **Behavior**: Opens target subsystem in new browser tab (`_blank` target)

### 3.3 State Management Bridge

**User Context Store Definition:**
```typescript
// Source: src/stores/user.ts:4-37
export const useUserStore = defineStore('user', () => {
  const userId = ref<number | null>(storedUserId ? Number(storedUserId) : null)
  // ... authentication state management
})
```

**Persistence Layer:**
- **Storage**: Browser `localStorage` for session persistence
- **Key**: `userId` (numeric identifier)
- **Lifecycle**: Cleared on explicit logout (`logout()` method)

---

## 4. Security Considerations

### 4.1 Current Implementation Model

**Trusted Network Perimeter Approach:**
- The current implementation assumes both subsystems operate within a controlled network environment
- Identity parameters are transmitted via URL query strings without cryptographic verification
- Suitable for demonstration, prototyping, and controlled academic environments

### 4.2 Production Hardening Recommendations

For enterprise-grade deployments, the following security enhancements are recommended:

1. **Token-Based Authentication:**
   - Migrate to JSON Web Tokens (JWT) with cryptographic signing
   - Implement token expiration and refresh mechanisms

2. **Server-Side Session Validation:**
   - Establish inter-service session verification via shared authentication service
   - Implement API Gateway pattern with OAuth 2.0 / OpenID Connect

3. **HTTPS Enforcement:**
   - Mandate TLS/SSL for all inter-subsystem communication
   - Prevent man-in-the-middle identity tampering

4. **Input Sanitization:**
   - Validate and sanitize `studentId` parameter on the receiving subsystem
   - Implement rate limiting to prevent identity enumeration attacks

---

## 5. Benefits & Trade-offs Analysis

### 5.1 Strategic Advantages

| Benefit | Technical Impact |
|---------|-----------------|
| **Accelerated Joint Delivery** | Enables parallel development streams without backend integration overhead |
| **Deployment Independence** | Each team maintains autonomous CI/CD pipelines and version control strategies |
| **Fault Isolation** | Service failures contained within module boundaries; no cascading outages |
| **Technology Heterogeneity** | Subsystems can adopt different technology stacks (e.g., different frontend frameworks) |

### 5.2 Acknowledged Limitations

| Limitation | Mitigation Strategy |
|-----------|-------------------|
| **Session State Fragmentation** | Browser tab isolation prevents shared session state; future migration to centralized session store recommended |
| **Network Latency** | Client-side redirects introduce minor latency (~200-500ms) compared to server-side routing |
| **Security Surface Area** | URL-based identity transfer vulnerable to parameter tampering; requires JWT implementation for production |
| **Operational Complexity** | Distributed tracing and logging more complex than monolithic architecture |

---

## 6. Future Evolution Roadmap

### Phase 1: Current State (MVP)
✅ Client-side hyperlink integration with query parameter identity passing
✅ Independent subsystem deployments

### Phase 2: Intermediate Hardening (Production Readiness)
- [ ] Implement JWT-based identity tokens
- [ ] Establish shared authentication service
- [ ] Deploy API Gateway with request routing

### Phase 3: Advanced Integration (Enterprise Scale)
- [ ] Migrate to iframe-based micro-frontend architecture with `single-spa` framework
- [ ] Implement server-side composition with Nginx reverse proxy
- [ ] Deploy distributed tracing with OpenTelemetry
- [ ] Establish centralized logging aggregation (ELK stack)

---

## 7. Technical Dependencies

### 7.1 Frontend Stack
- **Framework**: Vue 3 (Composition API)
- **State Management**: Pinia (`useUserStore`)
- **UI Component Library**: Element Plus
- **Routing**: Vue Router 4

### 7.2 Inter-Service Communication
- **Protocol**: HTTP/HTTPS
- **Method**: Client-side redirect (`window.open`)
- **Data Format**: URL query string parameters

---

## 8. References

### Code Artifacts
- `src/views/course/index.vue:405-409` - Navigation handler implementation
- `src/stores/user.ts:4-37` - User authentication state management
- `docs/CROSS_SYSTEM_INTEGRATION_SPEC.md` - This document

### Related Documentation
- Element Plus Design System: https://element-plus.org
- Vue 3 Composition API: https://vuejs.org/guide/extras/composition-api-faq.html
- Pinia State Management: https://pinia.vuejs.org

---

## Appendix A: Implementation Checklist

- [x] Import `Monitor` icon from Element Plus icon library
- [x] Import `useUserStore` from Pinia store module
- [x] Initialize user store instance in component setup
- [x] Implement `navigateToExamSystem()` handler with identity retrieval
- [x] Add "进入AI考试系统" button to recommendations card header
- [x] Style button group with flexbox layout (`header-actions` class)
- [x] Configure button click event binding to navigation handler
- [x] Implement fallback logic for missing userId (default: 1)

---

**Document Status**: Final
**Approval Required From**: Project Lead, Technical Architect, Security Review Board
