CREATE DATABASE IF NOT EXISTS Cardify;

USE Cardify;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    userType VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS binders (
    binder_id INT AUTO_INCREMENT PRIMARY KEY,
    owner VARCHAR(50) NOT NULL,
    set_id VARCHAR(50) NOT NULL,
    set_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS cards (
    card_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    set_id VARCHAR(50),
    image_url VARCHAR(255),
    game_type VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS binder_items (
    binder_id INT,
    card_id VARCHAR(50),
    quantity INT DEFAULT 0,
    PRIMARY KEY (binder_id, card_id),
    FOREIGN KEY (binder_id) REFERENCES binders (binder_id),
    FOREIGN KEY (card_id) REFERENCES cards (card_id)
);

CREATE TABLE IF NOT EXISTS proposals (
    id VARCHAR(50) PRIMARY KEY,
    proposer_username VARCHAR(100) NOT NULL,
    receiver_username VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    meeting_store_username VARCHAR(100),
    scheduled_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proposer_username) REFERENCES users (username),
    FOREIGN KEY (receiver_username) REFERENCES users (username),
    FOREIGN KEY (meeting_store_username) REFERENCES users (username)
);

CREATE TABLE IF NOT EXISTS proposal_items (
    proposal_id VARCHAR(50) NOT NULL,
    item_id VARCHAR(50) NOT NULL,
    quantity INT DEFAULT 1,
    item_type VARCHAR(20) NOT NULL, -- 'OFFERED' or 'REQUESTED'
    PRIMARY KEY (
        proposal_id,
        item_id,
        item_type
    ),
    FOREIGN KEY (proposal_id) REFERENCES proposals (id),
    FOREIGN KEY (item_id) REFERENCES cards (card_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (username)
);

CREATE TABLE IF NOT EXISTS trade_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    proposer_id VARCHAR(100),
    receiver_id VARCHAR(100),
    store_id VARCHAR(100),
    status VARCHAR(50),
    trade_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    proposer_code INT,
    receiver_code INT,
    proposer_arrived BOOLEAN DEFAULT FALSE,
    receiver_arrived BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (proposer_id) REFERENCES users (username),
    FOREIGN KEY (receiver_id) REFERENCES users (username),
    FOREIGN KEY (store_id) REFERENCES users (username)
);

CREATE TABLE IF NOT EXISTS trade_session_items (
    session_id INT,
    card_id VARCHAR(50),
    item_type VARCHAR(20), -- 'OFFERED' or 'REQUESTED'
    quantity INT DEFAULT 1,
    PRIMARY KEY (
        session_id,
        card_id,
        item_type
    ),
    FOREIGN KEY (session_id) REFERENCES trade_sessions (session_id),
    FOREIGN KEY (card_id) REFERENCES cards (card_id)
);

SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS check_expired_items
ON SCHEDULE EVERY 30 MINUTE
DO
BEGIN
    UPDATE proposals 
    SET status = 'EXPIRED' 
    WHERE status = 'PENDING' AND scheduled_at < NOW();

    UPDATE trade_sessions 
    SET status = 'EXPIRED' 
    WHERE status = 'WAITING_FOR_ARRIVAL' AND trade_date < NOW();
END;