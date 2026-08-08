package com.oem.evwarranty.domain.customer;



import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.customer.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFullName("John Doe");
        customer.setEmail("john.doe@example.com");
    }

    @Test
    void createCustomer_NewEmail_Success() {
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer created = customerService.createCustomer(customer);

        assertNotNull(created);
        assertEquals(customer.getEmail(), created.getEmail());
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_ExistingEmail_ThrowsException() {
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.createCustomer(customer);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void findById_Found_ReturnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> found = customerService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getFullName());
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            customerService.updateCustomer(1L, customer);
        });
    }

    @Test
    void deleteCustomer_CallsRepository() {
        customerService.deleteCustomer(1L);
        verify(customerRepository).deleteById(1L);
    }
}
