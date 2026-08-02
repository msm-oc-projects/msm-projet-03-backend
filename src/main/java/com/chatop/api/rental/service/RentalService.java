package com.chatop.api.rental.service;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.dto.RentalsResponse;
import com.chatop.api.rental.dto.UpdateRentalRequest;
import com.chatop.api.rental.entity.RentalEntity;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.shared.error.ForbiddenOperationException;
import com.chatop.api.shared.error.ResourceNotFoundException;
import com.chatop.api.storage.FileStorageService;
import com.chatop.api.user.entity.UserEntity;
import com.chatop.api.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public RentalService(
            RentalRepository rentalRepository,
            UserService userService,
            FileStorageService fileStorageService
    ) {
        this.rentalRepository = rentalRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public RentalsResponse getAll() {
        return new RentalsResponse(rentalRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RentalResponse::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public RentalResponse getById(Long id) {
        return RentalResponse.from(findById(id));
    }

    @Transactional
    public void create(CreateRentalRequest request, String authenticatedEmail) {
        UserEntity owner = userService.findByEmail(authenticatedEmail);
        String pictureUrl = fileStorageService.storeImage(request.getPicture());
        RentalEntity rental = new RentalEntity(
                request.getName().trim(), request.getSurface(), request.getPrice(),
                pictureUrl, request.getDescription().trim(), owner
        );
        rentalRepository.save(rental);
    }

    @Transactional
    public void update(Long id, UpdateRentalRequest request, String authenticatedEmail) {
        RentalEntity rental = findById(id);
        UserEntity authenticatedUser = userService.findByEmail(authenticatedEmail);
        if (!rental.getOwner().getId().equals(authenticatedUser.getId())) {
            throw new ForbiddenOperationException("Seul le propriétaire peut modifier cette location");
        }
        rental.update(
                request.getName().trim(), request.getSurface(), request.getPrice(),
                request.getDescription().trim()
        );
    }

    @Transactional(readOnly = true)
    public RentalEntity findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }
}
