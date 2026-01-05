package org.mrp.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mrp.models.MediaEntry;
import org.mrp.utils.Database;

import java.sql.SQLException;
import java.util.UUID;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MediaRepositoryTest {

    private MediaRepository mediaRepository;
    private Database dbMock;

    @BeforeEach
    public void setUp() {
        dbMock = mock(Database.class); //create mock DB
        mediaRepository = new MediaRepository();
        mediaRepository.setDb(dbMock); //pass mocked DB
    }

    @Test
    public void testSave() throws SQLException {
        MediaEntry mediaEntry = new MediaEntry(UUID.randomUUID(), "Title", "Description", UUID.randomUUID(), 2020, 18, Arrays.asList("Drama", "Thriller"), "Movie");

        when(dbMock.insert(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mediaEntry.getId());

        UUID mediaId = mediaRepository.save(mediaEntry);

        assertNotNull(mediaId);
        verify(dbMock).insert(eq("INSERT INTO MediaEntries (media_id, title, description, creator, release_year, age_restriction, genres, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(mediaEntry.getTitle()),
                eq(mediaEntry.getDesc()),
                eq(mediaEntry.getCreator()),
                eq(mediaEntry.getReleaseYear()),
                eq(mediaEntry.getAgeRestriction()),
                anyString(),
                eq(mediaEntry.getType()));
    }

    @Test
    public void testUpdate() throws SQLException {
        MediaEntry mediaEntry = new MediaEntry(UUID.randomUUID(), "Updated Title", "Updated Description", UUID.randomUUID(), 2021, 16, Arrays.asList("Action"), "Series");

        mediaRepository.update(mediaEntry);

        verify(dbMock).update(eq("UPDATE MediaEntries SET title = ?, description = ?, release_year = ?, age_restriction = ?, genres = ?, type = ? WHERE media_id = ?"),
                eq(mediaEntry.getTitle()),
                eq(mediaEntry.getDesc()),
                eq(mediaEntry.getReleaseYear()),
                eq(mediaEntry.getAgeRestriction()),
                anyString(),
                eq(mediaEntry.getType()),
                eq(mediaEntry.getId()));
    }

    @Test
    public void testDelete() throws SQLException {
        UUID mediaId = UUID.randomUUID();

        mediaRepository.delete(mediaId);

        verify(dbMock).update(eq("DELETE FROM MediaEntries WHERE media_id = ?"), eq(mediaId));
    }

//    @Test
//    public void testGetAll() throws SQLException {
//        MediaEntry mediaEntry = new MediaEntry(UUID.randomUUID(), "Title", "Description", UUID.randomUUID(), 2020, 18, Arrays.asList("Drama"), "Movie");
//        when(dbMock.query(anyString())).thenReturn(createMockResultSet(List.of(mediaEntry)));
//
//        List<Object> result = mediaRepository.getAll();
//
//        assertEquals(1, result.size());
//        assertInstanceOf(MediaEntry.class, result.get(0));
//        MediaEntry resultEntry = (MediaEntry) result.get(0);
//        assertEquals(mediaEntry.getTitle(), resultEntry.getTitle());
//    }
//
//    @Test
//    public void testGetOne() throws SQLException {
//        UUID mediaId = UUID.randomUUID();
//        MediaEntry mediaEntry = new MediaEntry(mediaId, "Title", "Description", UUID.randomUUID(), 2020, 18, Arrays.asList("Drama"), "Movie");
//        when(dbMock.query(anyString(), eq(mediaId))).thenReturn(createMockResultSet(List.of(mediaEntry)));
//
//        Object result = mediaRepository.getOne(mediaId);
//
//        assertNotNull(result);
//        assertInstanceOf(MediaEntry.class, result);
//        MediaEntry resultEntry = (MediaEntry) result;
//        assertEquals(mediaEntry.getTitle(), resultEntry.getTitle());
//    }
//
//    @Test
//    public void testChkCreator() throws SQLException {
//        UUID mediaId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//        when(dbMock.query(anyString(), eq(mediaId))).thenReturn(createMockResultSetWithCreator(userId));
//
//        boolean result = mediaRepository.chkCreator(mediaId, userId);
//
//        assertTrue(result);
//    }
//
//    private ResultSet createMockResultSet(List<MediaEntry> mediaEntries) throws SQLException {
//        ResultSet rs = mock(ResultSet.class);
//
//        when(rs.next()).thenReturn(true, false); //mock one row in rs
//
//        //specify data to be returned
//        when(rs.getObject("media_id")).thenReturn(mediaEntries.get(0).getId());
//        when(rs.getString("title")).thenReturn(mediaEntries.get(0).getTitle());
//        when(rs.getString("description")).thenReturn(mediaEntries.get(0).getDesc());
//        when(rs.getObject("creator")).thenReturn(mediaEntries.get(0).getCreator());
//        when(rs.getInt("release_year")).thenReturn(mediaEntries.get(0).getReleaseYear());
//        when(rs.getInt("age_restriction")).thenReturn(mediaEntries.get(0).getAgeRestriction());
//        when(rs.getString("genres")).thenReturn("Drama");
//        when(rs.getString("type")).thenReturn(mediaEntries.get(0).getType());
//
//        return rs;
//    }
//
//    private ResultSet createMockResultSetWithCreator(UUID userId) throws SQLException {
//        ResultSet rs = mock(ResultSet.class);   //mock rs
//
//        when(rs.next()).thenReturn(true, false); //mock one row returned
//
//        when(rs.getString("creator")).thenReturn(userId.toString());    //specify userId to be returned
//
//        return rs;
//    }

}
