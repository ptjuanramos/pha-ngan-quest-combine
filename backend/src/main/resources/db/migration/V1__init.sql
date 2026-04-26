DECLARE @now DATETIME2 = SYSDATETIME();

-- ============================================================
-- Tables
-- ============================================================

CREATE TABLE players (
    id         BIGINT        IDENTITY(1,1) PRIMARY KEY,
    username   NVARCHAR(255) NOT NULL,
    is_admin   BIT           NOT NULL DEFAULT 0,
    created_at DATETIME2     NOT NULL,
    updated_at DATETIME2     NOT NULL,
    CONSTRAINT uq_players_username UNIQUE (username)
);

CREATE TABLE missions (
    id                  INT            PRIMARY KEY,
    title               NVARCHAR(255)  NOT NULL,
    clue                NVARCHAR(1000) NOT NULL,
    location_hint       NVARCHAR(500)  NOT NULL,
    challenge           NVARCHAR(1000) NOT NULL,
    is_spicy            BIT            NOT NULL DEFAULT 0,
    validation_keywords NVARCHAR(500)  NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL
);

CREATE TABLE mission_completions (
    id           BIGINT    IDENTITY(1,1) PRIMARY KEY,
    player_id    BIGINT    NOT NULL REFERENCES players(id),
    mission_id   INT       NOT NULL REFERENCES missions(id),
    completed_at DATETIME2 NOT NULL,
    created_at   DATETIME2 NOT NULL,
    updated_at   DATETIME2 NOT NULL,
    CONSTRAINT uq_mission_completions_player_mission UNIQUE (player_id, mission_id)
);

CREATE TABLE photos (
    id                BIGINT         IDENTITY(1,1) PRIMARY KEY,
    player_id         BIGINT         NOT NULL REFERENCES players(id),
    mission_id        INT            NOT NULL REFERENCES missions(id),
    blob_path         NVARCHAR(1000) NOT NULL,
    sas_token         NVARCHAR(MAX)  NULL,
    sas_expires_at    DATETIME2      NULL,
    validation_status NVARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at        DATETIME2      NOT NULL,
    updated_at        DATETIME2      NOT NULL,
    CONSTRAINT uq_photos_player_mission UNIQUE (player_id, mission_id)
);

-- ============================================================
-- Seed: players
-- ============================================================

INSERT INTO players (username, is_admin, created_at, updated_at) VALUES
(N'godmod',    1, @now, @now),
(N'elchico',   0, @now, @now),
(N'coelhinha', 0, @now, @now);

-- ============================================================
-- Seed: missions
-- ============================================================

INSERT INTO missions (id, title, clue, location_hint, challenge, is_spicy, validation_keywords, created_at, updated_at) VALUES
(1, N'Exploradora da Praia',
    N'Toda aventura numa ilha começa onde a terra encontra o mar.',
    N'Uma praia bonita com vista para o oceano',
    N'Tira uma selfie numa praia bonita com o oceano atrás de ti.',
    0, N'beach,ocean,sea,sand,water,coast,person', @now, @now),

(2, N'Caçadora de Cocos',
    N'Nesta ilha, o tesouro cresce bem acima da tua cabeça.',
    N'Um coqueiro ou barraquinha local',
    N'Encontra um coco fresco ou uma barraquinha de cocos e tira uma foto.',
    0, N'coconut,palm,tropical,fruit', @now, @now),

(3, N'Desbravadora da Selva',
    N'Deixa a praia e entra no selvagem.',
    N'Um trilho na selva ou caminho tropical',
    N'Encontra um trilho na selva, plantas tropicais ou um caminho e tira uma foto lá.',
    0, N'forest,jungle,tree,vegetation,path,trail', @now, @now),

(4, N'Caçadora de Miradouros',
    N'Para entender uma ilha, tens de a ver de cima.',
    N'Um miradouro com vista para a ilha',
    N'Chega a um miradouro e tira uma foto da paisagem da ilha.',
    0, N'landscape,panorama,hill,mountain,island,sky', @now, @now),

(5, N'Descoberta Local',
    N'A cultura de um lugar é saboreada antes de ser compreendida.',
    N'Um mercado local ou vendedor de rua',
    N'Tira uma foto de um prato tailandês local ou comida de rua que descobrires.',
    0, N'food,dish,plate,market,cuisine,meal,street', @now, @now),

(6, N'Lugar Secreto',
    N'Os melhores lugares raramente são os mais movimentados.',
    N'Um cantinho tranquilo e secreto da ilha',
    N'Encontra um lugar calmo ou escondido na ilha e tira uma foto.',
    0, N'water,nature,beach,cove,landscape,scenic', @now, @now),

(7, N'Desafio Picante',
    N'A aventura é experimentar coisas novas… às vezes fora da tua zona de conforto.',
    N'Onde a tua coragem te levar',
    N'Tira uma foto divertida ou ousada — saltar para a água, uma pose engraçada, um pequeno desafio seguro mas divertido.',
    1, N'person,water,outdoor,jump,fun', @now, @now),

(8, N'Final Picante',
    N'O tesouro final aparece quando o sol desaparece… e o coração acelera.',
    N'Um lugar com pôr do sol na ilha',
    N'Tira uma foto ao pôr do sol que seja divertida, atrevida ou ligeiramente ousada — uma silhueta, uma pose cheia de charme, algo memorável.',
    1, N'sunset,sky,orange,golden,horizon,silhouette,dusk', @now, @now);