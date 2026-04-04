package com.aaronjosh.real_estate_app.models;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "files")
@Data
@ToString(exclude = { "propertyEntity" })
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String filename;
    private Long size;
    private String type;
    private String contentType;
    private String key;
    private LocalDateTime uploadedAt;

    public FileEntity() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = true)
    private PropertyEntity propertyEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = true)
    private MessageEntity message;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();

        if (propertyEntity == null && message == null) {
            throw new IllegalArgumentException(
                    "FileEntity must be associated with either a PropertyEntity or a MessageEntity.");
        }

        if (propertyEntity != null && message != null) {
            throw new IllegalArgumentException(
                    "FileEntity cannot be associated with both a PropertyEntity and a MessageEntity.");
        }
    }
}
