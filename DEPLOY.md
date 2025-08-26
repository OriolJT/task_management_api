# Deploy

This document covers minimal production-ish deployment options for the API.

## Docker (single container)

Prereqs: Java 21 not required on host; only Docker needed.

Build image (multi-stage Dockerfile builds the JAR first):

```powershell
# From repo root
docker build -t task-api:latest .
```

If the build fails with "permission denied" running gradlew inside the image, add an executable bit in your Dockerfile before the Gradle step:

```dockerfile
# add before RUN ./gradlew ...
RUN chmod +x ./gradlew
```

Run with a managed Postgres & Keycloak (external services) by setting envs:

```powershell
docker run --rm -p 8080:8080 ^
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/task_manager ^
  -e SPRING_DATASOURCE_USERNAME=<user> ^
  -e SPRING_DATASOURCE_PASSWORD=<pass> ^
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://<kc-host>/realms/<realm> ^
  -e APP_SECURITY_OAUTH2_CLIENT_ID=task-api ^
  -e SPRING_PROFILES_ACTIVE=prod ^
  -e APP_SEED_USER_ENABLED=false ^
  -e SPRING_JPA_SHOW_SQL=false ^
  task-api:latest
```

Notes:
- The app also supports KEYCLOAK_ISSUER_URI as a convenience; using the standard SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI is recommended.
- APP_SECURITY_OAUTH2_CLIENT_ID is optional and only used for mapping client roles from Keycloak tokens.

Alternatively, use the provided docker-compose for local dev (app+db+keycloak):

```powershell
docker-compose up --build
```

See compose file: [docker-compose.yml](./docker-compose.yml)

## Helm (Kubernetes)

Below is a minimal chart skeleton you can copy into `helm/task-api`.

```
helm/
  task-api/
    Chart.yaml
    values.yaml
    templates/
      deployment.yaml
      service.yaml
      ingress.yaml
```

Example files:

- Chart.yaml
  ```yaml
  apiVersion: v2
  name: task-api
  description: Task Management API
  type: application
  version: 0.1.0
  appVersion: "0.0.1"
  ```

- values.yaml
  ```yaml
  image:
    repository: your-docker-registry/task-api
    tag: latest
    pullPolicy: IfNotPresent

  service:
    type: ClusterIP
    port: 8080

  env:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres.default.svc.cluster.local:5432/task_manager
    SPRING_DATASOURCE_USERNAME: postgres
    # Use a Secret for the password; see deployment.yaml for valueFrom example
    SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: https://keycloak.example.com/realms/task-realm
    APP_SECURITY_OAUTH2_CLIENT_ID: task-api # optional
    SPRING_PROFILES_ACTIVE: prod
    APP_SEED_USER_ENABLED: "false"
    SPRING_JPA_SHOW_SQL: "false"
  ```

- templates/deployment.yaml
  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: task-api
  spec:
    replicas: 2
    revisionHistoryLimit: 3
    strategy:
      type: RollingUpdate
      rollingUpdate:
        maxSurge: 25%
        maxUnavailable: 25%
    selector:
      matchLabels:
        app: task-api
    template:
      metadata:
        labels:
          app: task-api
      spec:
        securityContext:
          runAsNonRoot: true
          fsGroup: 1000
        containers:
          - name: task-api
            image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
            imagePullPolicy: {{ .Values.image.pullPolicy }}
            ports:
              - containerPort: 8080
            env:
              - name: SPRING_DATASOURCE_URL
                value: "{{ .Values.env.SPRING_DATASOURCE_URL }}"
              - name: SPRING_DATASOURCE_USERNAME
                value: "{{ .Values.env.SPRING_DATASOURCE_USERNAME }}"
              - name: SPRING_DATASOURCE_PASSWORD
                valueFrom:
                  secretKeyRef:
                    name: task-api-secrets
                    key: SPRING_DATASOURCE_PASSWORD
              - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI
                value: "{{ .Values.env.SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI }}"
              {{- if .Values.env.APP_SECURITY_OAUTH2_CLIENT_ID }}
              - name: APP_SECURITY_OAUTH2_CLIENT_ID
                value: "{{ .Values.env.APP_SECURITY_OAUTH2_CLIENT_ID }}"
              {{- end }}
              - name: SPRING_PROFILES_ACTIVE
                value: "{{ .Values.env.SPRING_PROFILES_ACTIVE }}"
              - name: APP_SEED_USER_ENABLED
                value: "{{ .Values.env.APP_SEED_USER_ENABLED }}"
              - name: SPRING_JPA_SHOW_SQL
                value: "{{ .Values.env.SPRING_JPA_SHOW_SQL }}"
            # Probes
            # Option A (recommended): enable Spring Boot Actuator and probe health endpoints
            # readinessProbe:
            #   httpGet:
            #     path: /actuator/health/readiness
            #     port: 8080
            # livenessProbe:
            #   httpGet:
            #     path: /actuator/health/liveness
            #     port: 8080

            # Option B (no Actuator): use a TCP socket probe (lightweight)
            readinessProbe:
              tcpSocket:
                port: 8080
            livenessProbe:
              tcpSocket:
                port: 8080
            resources:
              requests:
                cpu: 100m
                memory: 256Mi
              limits:
                cpu: 500m
                memory: 512Mi
            securityContext:
              readOnlyRootFilesystem: true
              allowPrivilegeEscalation: false
              capabilities:
                drop: ["ALL"]
  ```

- templates/service.yaml
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: task-api
  spec:
    type: {{ .Values.service.type }}
    selector:
      app: task-api
    ports:
      - name: http
        port: {{ .Values.service.port }}
        targetPort: 8080
        protocol: TCP
  ```

Optionally, manage sensitive settings with a Secret:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: task-api-secrets
type: Opaque
stringData:
  SPRING_DATASOURCE_PASSWORD: postgres
```

Then reference it in `templates/deployment.yaml`:

```yaml
env:
  - name: SPRING_DATASOURCE_URL
    value: "{{ .Values.env.SPRING_DATASOURCE_URL }}"
  - name: SPRING_DATASOURCE_USERNAME
    value: "{{ .Values.env.SPRING_DATASOURCE_USERNAME }}"
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: task-api-secrets
        key: SPRING_DATASOURCE_PASSWORD
  - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI
    value: "{{ .Values.env.SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI }}"
  {{- if .Values.env.APP_SECURITY_OAUTH2_CLIENT_ID }}
  - name: APP_SECURITY_OAUTH2_CLIENT_ID
    value: "{{ .Values.env.APP_SECURITY_OAUTH2_CLIENT_ID }}"
  {{- end }}
  - name: SPRING_PROFILES_ACTIVE
    value: "{{ .Values.env.SPRING_PROFILES_ACTIVE }}"
  - name: APP_SEED_USER_ENABLED
    value: "{{ .Values.env.APP_SEED_USER_ENABLED }}"
  - name: SPRING_JPA_SHOW_SQL
    value: "{{ .Values.env.SPRING_JPA_SHOW_SQL }}"
```

Install:

```powershell
helm upgrade --install task-api ./helm/task-api --namespace default
```

Notes:
- If you enable Spring Boot Actuator, switch probes to `/actuator/health/readiness` and `/actuator/health/liveness`.
- Configure NetworkPolicy/Ingress and TLS as appropriate in your cluster.
- For production, externalize secrets and use Kubernetes Secrets.
- Flyway migrations run automatically on startup; ensure DB connectivity and privileges are correct.

- Private registries: add imagePullSecrets to your Pod spec
  ```yaml
  spec:
    imagePullSecrets:
      - name: my-regcred
  ```

- Example `templates/ingress.yaml` (stub):
  ```yaml
  apiVersion: networking.k8s.io/v1
  kind: Ingress
  metadata:
    name: task-api
    annotations:
      nginx.ingress.kubernetes.io/proxy-body-size: "10m"
  spec:
    ingressClassName: nginx
    rules:
      - host: task-api.example.com
        http:
          paths:
            - path: /
              pathType: Prefix
              backend:
                service:
                  name: task-api
                  port:
                    name: http
    tls:
      - hosts:
          - task-api.example.com
        secretName: task-api-tls
  ```
