package studio.studioeye.domain;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import studio.studioeye.domain.client.application.ClientService;
import studio.studioeye.domain.client.dao.ClientRepository;
import studio.studioeye.infrastructure.s3.S3Adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyInformationServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private S3Adapter s3Adapter;

    private final MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "testLogo.jpg",
            "image/jpeg",
            "Test Logo Content".getBytes()
    );
    
}
