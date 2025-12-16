CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ===================== USERS =====================

CREATE TABLE users (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username    TEXT NOT NULL UNIQUE,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ===================== TOKENS (SESSIONS) =====================

CREATE TABLE auth_tokens (
                             token_hash   BYTEA PRIMARY KEY,
                             user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                             expires_at   TIMESTAMPTZ NOT NULL,
                             revoked_at   TIMESTAMPTZ,
                             last_used_at TIMESTAMPTZ
);

CREATE INDEX idx_auth_tokens_user     ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_expires  ON auth_tokens(expires_at);
CREATE INDEX idx_auth_tokens_revoked  ON auth_tokens(revoked_at);

-- ===================== TASKS =====================

CREATE TABLE tasks (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                       title         TEXT NOT NULL,
                       description   TEXT NOT NULL,

                       created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                       deadline      TIMESTAMPTZ,
                       completed_at  TIMESTAMPTZ,

                       CONSTRAINT deadline_after_creation
                           CHECK (deadline IS NULL OR deadline >= created_at),

                       CONSTRAINT completed_after_creation
                           CHECK (completed_at IS NULL OR completed_at >= created_at)
);

CREATE INDEX idx_tasks_user_created   ON tasks(user_id, created_at DESC);
CREATE INDEX idx_tasks_user_deadline  ON tasks(user_id, deadline);
CREATE INDEX idx_tasks_user_completed ON tasks(user_id, completed_at);
