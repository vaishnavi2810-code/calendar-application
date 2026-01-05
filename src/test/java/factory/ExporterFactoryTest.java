package factory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.dto.ExportEventDto;
import calendar.factory.ExporterFactory;
import calendar.interfacetypes.Iexport;
import calendar.strategy.GoogleCsvExporter;
import calendar.strategy.IcalExporter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the ExporterFactory class for creating appropriate exporter instances
 * based on file extensions and handling invalid inputs.
 */
public class ExporterFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testGetExporterCsvExtensionReturnsGoogleCsvExporter() throws Exception {
    ExportEventDto dto = new ExportEventDto("events.csv");
    Iexport exporter = ExporterFactory.getExporter(dto);
    assertNotNull("Exporter should not be null", exporter);
    assertTrue("Should return GoogleCsvExporter instance",
            exporter instanceof GoogleCsvExporter);
  }

  @Test
  public void testGetExporterIcalExtensionReturnsIcalExporter() throws Exception {
    ExportEventDto dto = new ExportEventDto("events.ical");
    Iexport exporter = ExporterFactory.getExporter(dto);
    assertNotNull("Exporter should not be null", exporter);
    assertTrue("Should return IcalExporter instance",
            exporter instanceof IcalExporter);
  }

  @Test
  public void testGetExporterIcsExtensionReturnsIcalExporter() throws Exception {
    ExportEventDto dto = new ExportEventDto("events.ics");
    Iexport exporter = ExporterFactory.getExporter(dto);
    assertNotNull("Exporter should not be null", exporter);
    assertTrue("Should return IcalExporter instance",
            exporter instanceof IcalExporter);
  }

  @Test
  public void testGetExporterCsvUpperCaseReturnsGoogleCsvExporter() throws Exception {
    ExportEventDto dto = new ExportEventDto("EVENTS.CSV");
    Iexport exporter = ExporterFactory.getExporter(dto);
    assertNotNull("Exporter should not be null", exporter);
    assertTrue("Should return GoogleCsvExporter instance",
            exporter instanceof GoogleCsvExporter);
  }

  @Test
  public void testGetExporterCsvMixedCaseReturnsGoogleCsvExporter() throws Exception {
    ExportEventDto dto = new ExportEventDto("MyEvents.Csv");
    Iexport exporter = ExporterFactory.getExporter(dto);
    assertNotNull("Exporter should not be null", exporter);
    assertTrue("Should return GoogleCsvExporter instance",
            exporter instanceof GoogleCsvExporter);
  }

  @Test
  public void testGetExporterNullFileNameThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto(null);
    thrown.expect(Exception.class);
    thrown.expectMessage("Invalid file name.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testGetExporterFileNameWithoutExtensionThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto("exportfile");
    thrown.expect(Exception.class);
    thrown.expectMessage("Invalid file name or missing extension.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testGetExporterEmptyFileNameThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto("");
    thrown.expect(Exception.class);
    thrown.expectMessage("Invalid file name.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testGetExporterUnsupportedFileTypeThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto("document.pdf");
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: File type '.pdf' is not supported.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testGetExporterXmlFileTypeThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto("data.xml");
    thrown.expect(Exception.class);
    thrown.expectMessage("Error: File type '.xml' is not supported.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testGetExporterMultipleDotsInFileNameWorks() throws Exception {
    ExportEventDto dto = new ExportEventDto("my.export.file.csv");
    Iexport exporter = ExporterFactory.getExporter(dto);
    assertNotNull("Exporter should not be null", exporter);
    assertTrue("Should return GoogleCsvExporter instance",
            exporter instanceof GoogleCsvExporter);
  }

  @Test
  public void testGetExporterFileNameJustDotThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto(".");
    thrown.expect(Exception.class);
    thrown.expectMessage("Invalid file name or missing extension.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testGetExporterFileNameDotAtStartThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto(".configfile");
    thrown.expect(Exception.class);
    thrown.expectMessage("Invalid file name or missing extension.");
    ExporterFactory.getExporter(dto);
  }

  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<ExporterFactory> constructor =
            ExporterFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    ExporterFactory factory = constructor.newInstance();
    assertNotNull(factory);
  }

  @Test
  public void testGetExporterFileNameEndsWithDotThrowsException() throws Exception {
    ExportEventDto dto = new ExportEventDto("events.");
    thrown.expect(Exception.class);
    thrown.expectMessage("Invalid file name or missing extension.");
    ExporterFactory.getExporter(dto);
  }

}