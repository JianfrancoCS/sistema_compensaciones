import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthStore } from '@core/store/auth.store';

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.html',
  standalone: true,
  styleUrl: './home.css'
})
export class Home implements OnInit, OnDestroy {
  private authStore = inject(AuthStore);

  currentSlide = 0;
  slides = [
    {
      title: 'Transforma tu producción agrícola',
      subtitle: 'Tecnología de última generación para el agro moderno',
      image: '/images/carusel-arandano.jpg'
    },
    {
      title: 'Automatiza y optimiza tus recursos',
      subtitle: 'Gestión inteligente para aumentar tu productividad',
      image: '/images/carusel-palta.jpg'
    },
    {
      title: 'Conecta innovación con sostenibilidad',
      subtitle: 'Un futuro más eficiente y responsable con la tierra',
      image: '/images/carusel-uva.jpg'
    }
  ];

  features = [
    {
      icon: 'pi-users',
      title: 'Gestión de Personal',
      description: 'Administra empleados, contratos y roles de forma eficiente con un sistema centralizado.',
      color: '#4faf5a'
    },
    {
      icon: 'pi-calendar',
      title: 'Control de Asistencia',
      description: 'Registra y monitorea la asistencia del personal en tiempo real con códigos QR.',
      color: '#4faf5a'
    },
    {
      icon: 'pi-wallet',
      title: 'Planillas Automatizadas',
      description: 'Genera planillas de pago precisas basadas en asistencia y configuraciones personalizadas.',
      color: '#4faf5a'
    },
    {
      icon: 'pi-chart-line',
      title: 'Reportes y Análisis',
      description: 'Visualiza métricas clave y genera reportes detallados para tomar mejores decisiones.',
      color: '#4faf5a'
    },
    {
      icon: 'pi-file-edit',
      title: 'Contratos Digitales',
      description: 'Crea, edita y gestiona contratos laborales con plantillas personalizables.',
      color: '#4faf5a'
    },
    {
      icon: 'pi-map-marker',
      title: 'Gestión de Ubicaciones',
      description: 'Organiza y administra múltiples sucursales, áreas y unidades laborales.',
      color: '#4faf5a'
    }
  ];

  private autoSlideInterval: any;

  ngOnInit() {
    this.startAutoSlide();
  }

  ngOnDestroy() {
    if (this.autoSlideInterval) {
      clearInterval(this.autoSlideInterval);
    }
  }

  startAutoSlide() {
    this.autoSlideInterval = setInterval(() => this.nextSlide(), 5000);
  }

  nextSlide() {
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;
  }

  previousSlide() {
    this.currentSlide = (this.currentSlide === 0) ? this.slides.length - 1 : this.currentSlide - 1;
  }

  goToSlide(index: number) {
    this.currentSlide = index;
  }

  scrollToFeatures() {
    document.getElementById('caracteristicas')?.scrollIntoView({ behavior: 'smooth' });
  }

  login(): void {
    window.location.href = '/login';
  }
}
