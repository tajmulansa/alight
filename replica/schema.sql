-- Local SQLite schema for the clone (Android, Room-compatible types).
-- Single user, no network. Times are integer microseconds from project start.
-- Colours are 4 scalar channels (r,g,b,a 0..1) so every value is a number and keyframeable.
PRAGMA foreign_keys = ON;

CREATE TABLE project (
  id            TEXT PRIMARY KEY,                       -- uuid made in the app
  kind          TEXT NOT NULL DEFAULT 'project' CHECK (kind IN ('project','element')),
  name          TEXT NOT NULL,
  width         INTEGER NOT NULL CHECK (width  BETWEEN 16 AND 4096),
  height        INTEGER NOT NULL CHECK (height BETWEEN 16 AND 4096),
  frame_rate    REAL    NOT NULL DEFAULT 30 CHECK (frame_rate > 0 AND frame_rate <= 120),
  duration_us   INTEGER NOT NULL DEFAULT 5000000 CHECK (duration_us > 0),
  background    INTEGER NOT NULL DEFAULT 0,             -- ARGB int
  thumbnail     TEXT,                                   -- relative file path
  created_at    INTEGER NOT NULL,                       -- epoch ms, UTC
  updated_at    INTEGER NOT NULL
);
CREATE INDEX idx_project_kind_updated ON project (kind, updated_at DESC);

CREATE TABLE media (
  id            TEXT PRIMARY KEY,
  project_id    TEXT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  kind          TEXT NOT NULL CHECK (kind IN ('image','video','audio','font')),
  path          TEXT NOT NULL,                          -- copy inside app storage
  original_name TEXT,
  duration_us   INTEGER,                                -- video, audio
  width         INTEGER,
  height        INTEGER,
  created_at    INTEGER NOT NULL
);
CREATE INDEX idx_media_project ON media (project_id, kind);

CREATE TABLE layer (
  id                TEXT PRIMARY KEY,
  project_id        TEXT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  type              TEXT NOT NULL CHECK (type IN
                    ('shape','text','image','video','audio','null','camera','group','element')),
  name              TEXT NOT NULL,
  z_order           INTEGER NOT NULL,                   -- higher draws on top
  start_us          INTEGER NOT NULL DEFAULT 0 CHECK (start_us >= 0),
  end_us            INTEGER NOT NULL,
  media_offset_us   INTEGER NOT NULL DEFAULT 0,         -- trim into the source media
  visible           INTEGER NOT NULL DEFAULT 1 CHECK (visible IN (0,1)),
  locked            INTEGER NOT NULL DEFAULT 0 CHECK (locked IN (0,1)),
  parent_layer_id   TEXT REFERENCES layer(id) ON DELETE SET NULL,  -- transform parent, one only
  group_id          TEXT REFERENCES layer(id) ON DELETE CASCADE,   -- member of a group layer
  media_id          TEXT REFERENCES media(id) ON DELETE RESTRICT,
  nested_project_id TEXT REFERENCES project(id) ON DELETE RESTRICT, -- element layer only
  content_json      TEXT NOT NULL DEFAULT '{}',         -- static data: text, font, shape kind, path points
  created_at        INTEGER NOT NULL,
  updated_at        INTEGER NOT NULL,
  CHECK (end_us > start_us),
  CHECK (parent_layer_id IS NULL OR parent_layer_id <> id),
  CHECK (group_id IS NULL OR group_id <> id),
  CHECK (type NOT IN ('image','video','audio') OR media_id IS NOT NULL),
  CHECK (type <> 'element' OR nested_project_id IS NOT NULL)
);
CREATE INDEX idx_layer_project_z  ON layer (project_id, z_order);
CREATE INDEX idx_layer_parent     ON layer (parent_layer_id);
CREATE INDEX idx_layer_group      ON layer (group_id);
CREATE INDEX idx_layer_media      ON layer (media_id);
CREATE INDEX idx_layer_nested     ON layer (nested_project_id);

CREATE TABLE effect_instance (
  id          TEXT PRIMARY KEY,
  layer_id    TEXT NOT NULL REFERENCES layer(id) ON DELETE CASCADE,
  effect_type TEXT NOT NULL,                            -- 'fill','stroke','shadow','brightness_contrast',...
  order_index INTEGER NOT NULL,
  enabled     INTEGER NOT NULL DEFAULT 1 CHECK (enabled IN (0,1)),
  UNIQUE (layer_id, order_index)
);

CREATE TABLE mask (
  id        TEXT PRIMARY KEY,
  layer_id  TEXT NOT NULL REFERENCES layer(id) ON DELETE CASCADE,
  kind      TEXT NOT NULL CHECK (kind IN ('rect','ellipse','path')),
  path_data TEXT,                                       -- points for 'path'
  inverted  INTEGER NOT NULL DEFAULT 0 CHECK (inverted IN (0,1)),
  order_index INTEGER NOT NULL,
  UNIQUE (layer_id, order_index)
);

-- One animatable number. Owned by a layer or by an effect, never both.
-- path examples: 'position.x','position.y','scale.x','scale.y','rotation','opacity',
--                'anchor.x','fill.r','fill.g','fill.b','fill.a','shadow.blur'
CREATE TABLE track (
  id          TEXT PRIMARY KEY,
  layer_id    TEXT REFERENCES layer(id) ON DELETE CASCADE,
  effect_id   TEXT REFERENCES effect_instance(id) ON DELETE CASCADE,
  mask_id     TEXT REFERENCES mask(id) ON DELETE CASCADE,
  path        TEXT NOT NULL,
  base_value  REAL NOT NULL,                            -- used when there are no keyframes
  CHECK ((layer_id IS NOT NULL) + (effect_id IS NOT NULL) + (mask_id IS NOT NULL) = 1)
);
CREATE UNIQUE INDEX uq_track_layer  ON track (layer_id,  path) WHERE layer_id  IS NOT NULL;
CREATE UNIQUE INDEX uq_track_effect ON track (effect_id, path) WHERE effect_id IS NOT NULL;
CREATE UNIQUE INDEX uq_track_mask   ON track (mask_id,   path) WHERE mask_id   IS NOT NULL;

-- The curve on a keyframe shapes the motion from THIS keyframe to the next one.
-- Bezier handles may leave 0..1 on y (overshoot). Preset = bezier with fixed numbers.
CREATE TABLE keyframe (
  id          TEXT PRIMARY KEY,
  track_id    TEXT NOT NULL REFERENCES track(id) ON DELETE CASCADE,
  time_us     INTEGER NOT NULL CHECK (time_us >= 0),
  value       REAL NOT NULL,
  curve_type  TEXT NOT NULL DEFAULT 'linear' CHECK (curve_type IN
              ('linear','bezier','hold','bounce','elastic','cyclic','random','steps','elastic_steps')),
  c1x REAL NOT NULL DEFAULT 0.0 CHECK (c1x BETWEEN 0 AND 1),
  c1y REAL NOT NULL DEFAULT 0.0,
  c2x REAL NOT NULL DEFAULT 1.0 CHECK (c2x BETWEEN 0 AND 1),
  c2y REAL NOT NULL DEFAULT 1.0,
  curve_params TEXT NOT NULL DEFAULT '{}',              -- bounce count, steps, etc.
  UNIQUE (track_id, time_us)
);

CREATE TABLE bookmark (
  id          TEXT PRIMARY KEY,
  project_id  TEXT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  time_us     INTEGER NOT NULL CHECK (time_us >= 0),
  label       TEXT NOT NULL DEFAULT ''
);
CREATE INDEX idx_bookmark_project ON bookmark (project_id, time_us);

CREATE TABLE export_job (
  id          TEXT PRIMARY KEY,
  project_id  TEXT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  format      TEXT NOT NULL CHECK (format IN ('mp4_h264','mp4_hevc','gif','png_sequence','png_still')),
  width       INTEGER NOT NULL,
  height      INTEGER NOT NULL,
  frame_rate  REAL    NOT NULL,
  bitrate     INTEGER,                                  -- bits per second
  quality     REAL CHECK (quality BETWEEN 0 AND 1),
  status      TEXT NOT NULL DEFAULT 'queued' CHECK (status IN
              ('queued','running','done','failed','cancelled')),
  output_uri  TEXT,
  error       TEXT,
  created_at  INTEGER NOT NULL,
  finished_at INTEGER
);
CREATE INDEX idx_export_project ON export_job (project_id, created_at DESC);
CREATE INDEX idx_export_status  ON export_job (status);
