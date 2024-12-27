package antifraud.Services;

import antifraud.DTO.SuspiciousIPRequestDTO;
import antifraud.DTO.SuspiciousIPResponseDTO;
import antifraud.DTO.UserResponseDTO;
import antifraud.Errors.AlreadyExistsException;
import antifraud.Errors.BadFormatException;
import antifraud.Errors.UserNotFoundException;
import antifraud.Model.IP;
import antifraud.Model.User;
import antifraud.Repository.IPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IPService {
    private final IPRepository ipRepository;

    @Autowired
    public IPService(IPRepository ipRepository) {
        this.ipRepository = ipRepository;
    }
    public IP addSuspiciousIp(SuspiciousIPRequestDTO suspiciousIPRequestDTO) {
        if(ipRepository.findByIp(suspiciousIPRequestDTO.ip()).isPresent()) {
            throw new AlreadyExistsException();
        }
        if (isInvalidIp(suspiciousIPRequestDTO.ip())) {
            throw new BadFormatException();
        }

        IP ip = new IP(suspiciousIPRequestDTO.ip());
        ipRepository.save(ip);
        return ip;

    }

    public List<SuspiciousIPResponseDTO> getAllIp() {
        Iterable<IP> ips = ipRepository.findAll();
        List<SuspiciousIPResponseDTO> ipList = new ArrayList<>();
        for (IP ip : ips) {
            ipList.add(new SuspiciousIPResponseDTO(ip.getId(), ip.getIp()));
        }
        return ipList;
    }

    public IP deleteByIp(String ip) {
        Optional<IP> ipOptional = ipRepository.findByIp(ip);
        if (isInvalidIp(ip)) {
            throw new BadFormatException();
        }
        if (ipOptional.isEmpty()) {
            throw new UserNotFoundException();
        }
        IP ipToDelete = ipOptional.get();
        ipRepository.delete(ipToDelete);
        return ipToDelete;
    }

    public boolean isInvalidIp(String ip) {
        String[] splitIp = ip.split("\\.");
        if(splitIp.length != 4) {
            return true;
        }
        try {
            for (String s : splitIp) {
                int i = Integer.parseInt(s);
                if(i < 0 || i > 255) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }
}
